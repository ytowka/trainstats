package com.danilkha.trainstats

import android.content.Context
import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.pressBack
import com.danilkha.commoncore.viewmodel.LaunchCollectEffects
import com.danilkha.trainstats.features.exercises.ui.ExerciseListViewModel
import com.danilkha.trainstats.features.exercises.ui.selector.ExerciseSelectorBottomSheet
import org.koin.compose.viewmodel.koinViewModel
import com.danilkha.commoncore.utils.LocalDateFormat
import com.danilkha.commoncore.utils.rememberDateTimeFormatter
import com.danilkha.trainstats.features.workout.ui.exercisehistory.ExerciseHistoryBottomSheet
import com.danilkha.trainstats.features.workout.ui.exercisehistory.ExerciseHistoryBottomSheetArgs
import com.danilkha.commonds.theme.TrainingStatsTheme
import com.danilkha.trainstats.bottomsheet.BottomSheetHost
import com.danilkha.trainstats.bottomsheet.rememberBottomSheetState
import com.danilkha.trainstats.db.TrainStatsDb
import com.danilkha.trainstats.di.*
import com.danilkha.trainstats.features.exercises.data.db.RoomExerciseDatasource
import com.danilkha.trainstats.features.exercises.domain.model.*
import com.danilkha.trainstats.features.exercises.ui.editor.*
import com.danilkha.trainstats.features.exercises.ui.selector.VariationSelectorBottomSheet
import com.danilkha.trainstats.features.stats.domain.*
import com.danilkha.trainstats.features.stats.ui.*
import com.danilkha.trainstats.features.workout.data.db.RoomWorkoutDatasource
import com.danilkha.trainstats.features.workout.data.db.createTestDb
import com.danilkha.trainstats.features.workout.domain.model.*
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
class ExerciseVariationScreenTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var db: TrainStatsDb
    private lateinit var exercises: RoomExerciseDatasource
    private val narrow = ExerciseVariation("narrow", "pull", "Узкая ручка")
    private val wide = ExerciseVariation("wide", "pull", "Широкая ручка")
    private val pull = ExerciseData("pull", "тяга", null, false, true, variations = listOf(narrow, wide))
    private val workout = Workout("workout", Instant.parse("2026-10-10T12:00:00Z"), listOf(
        ExerciseSet("1", "workout", pull, Repetitions.Single(10f), Kg(100f), 0),
        ExerciseSet("2", "workout", pull, Repetitions.Single(10f), Kg(70f), 1, narrow.id, narrow.name),
    ), saved = true, archived = false)

    @Before fun setup(): Unit = runBlocking {
        stopKoin()
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = createTestDb(context)
        exercises = RoomExerciseDatasource(db)
        exercises.createExercise(pull)
        RoomWorkoutDatasource(db).saveWorkout(workout)
        startKoin {
            androidContext(context)
            modules(platformModule, dataModule, repositoryModule, useCaseModule, androidSharedModule, viewModelModule,
                module { single<TrainStatsDb> { db } })
        }
    }
    @After fun teardown() { stopKoin(); db.close() }

    @Test fun confirmedDeletionIsADraftUntilSaveAndClosingDiscardsIt() {
        rule.setContent {
            TrainingStatsTheme {
                BottomSheetHost {
                    val sheet = rememberBottomSheetState()
                    TextButton(onClick = { sheet.show(ExerciseEditorBottomSheetArgs.buildArgs(pull.id)) }) { Text("Открыть редактор") }
                    ExerciseEditorBottomSheet(sheet)
                }
            }
        }
        rule.onNodeWithText("Открыть редактор").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasText(narrow.name) and hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("variation_editor_row_narrow").performTouchInput { swipeLeft() }
        rule.onNodeWithText("Удалить вариацию?").assertIsDisplayed()
        rule.onNodeWithText("Удалить").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("variation_editor_row_narrow").fetchSemanticsNodes().isEmpty() }
        runBlocking { Assert.assertEquals(2, exercises.getExercise(pull.id).variations.size) }
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitUntil(5_000) { rule.onAllNodesWithText("Открыть редактор").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Открыть редактор").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasText(narrow.name) and hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("variation_editor_row_narrow").performTouchInput { swipeLeft() }
        rule.onNodeWithText("Удалить").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("variation_editor_row_narrow").fetchSemanticsNodes().isEmpty() }
        rule.onNode(hasText(wide.name) and hasSetTextAction()).performTextReplacement("Ручка 45°")
        rule.onNodeWithText("Сохранить").performScrollTo().performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithText("Открыть редактор").fetchSemanticsNodes().isNotEmpty() }
        runBlocking {
            Assert.assertEquals(listOf(wide.copy(name = "Ручка 45°")), exercises.getExercise(pull.id).variations)
            Assert.assertNull(RoomWorkoutDatasource(db).getWorkoutById(workout.id).steps.last().variationId)
        }
    }

    @Test fun creatingVariationFromWorkoutPersistsOnlyOnCreateAndClosesSelector() {
        rule.setContent {
            TrainingStatsTheme {
                BottomSheetHost {
                    var selected by remember { mutableStateOf<String?>(null) }
                    val sheet = rememberBottomSheetState()
                    Column {
                        TextButton(onClick = { sheet.show(mapOf("groupId" to 5L, "exerciseId" to pull.id)) }) { Text("Выбрать вариацию") }
                        selected?.let { Text("Выбрана: $it") }
                    }
                    VariationSelectorBottomSheet(sheet) { _, variation -> selected = variation?.name }
                }
            }
        }
        rule.onNodeWithText("Выбрать вариацию").performClick()
        rule.onNodeWithText("+ Добавить вариацию").performClick()
        rule.onNode(hasSetTextAction()).performTextInput("Ручка 45°")
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitUntil(5_000) { rule.onAllNodesWithText("Выбрать вариацию").fetchSemanticsNodes().isNotEmpty() }
        runBlocking { Assert.assertEquals(2, exercises.getExercise(pull.id).variations.size) }
        rule.onNodeWithText("Выбрать вариацию").performClick()
        rule.onNodeWithText("+ Добавить вариацию").performClick()
        rule.onNode(hasSetTextAction()).performTextInput("Ручка 45°")
        rule.onNodeWithText("Создать").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithText("Выбрана: Ручка 45°").fetchSemanticsNodes().isNotEmpty() }
        runBlocking { Assert.assertEquals(3, exercises.getExercise(pull.id).variations.size) }
    }

    @Test fun variationCheckboxesAreSharedBetweenChartsAndAllWorkoutsHidesTheFilter() {
        var state by mutableStateOf(StatisticsState(
            data = StatisticsData(listOf(pull), listOf(workout)), loading = false, exerciseId = pull.id,
        ))
        val vm = StatisticsViewModel(mockk())
        rule.setContent {
            TrainingStatsTheme {
                CompositionLocalProvider(LocalDateFormat provides rememberDateTimeFormatter()) {
                    StatisticsPage(state, { state = vm.reduce(state, it) }, {}, {})
                }
            }
        }
        rule.onNodeWithTag("statistics_variation_dropdown").assertTextContains("Вариация")
        val filterWidth = rule.onNodeWithTag("statistics_exercise_dropdown").fetchSemanticsNode().boundsInRoot.width
        rule.onNodeWithTag("statistics_variation_dropdown").performClick()
        Assert.assertEquals(filterWidth, rule.onNodeWithTag("statistics_variation_menu").fetchSemanticsNode().boundsInRoot.width, 1f)
        capture("variation-filter")
        rule.onNodeWithTag("statistics_variation_all").performClick()
        pressBack()
        rule.onAllNodesWithText("Выберите вариацию", useUnmergedTree = true)[0].assertExists()
        Assert.assertTrue(state.needsVariationSelection)
        rule.onNodeWithTag("statistics_variation_dropdown").performClick()
        rule.onNodeWithText(narrow.name).performClick()
        pressBack()
        Assert.assertEquals(70f, state.recordPoint!!.weight)
        rule.onNodeWithTag("statistics_list").performScrollToNode(hasText("Последний подход"))
        rule.onNodeWithText("Последний подход").performClick()
        rule.onNodeWithTag("statistics_list", useUnmergedTree = true).performScrollToNode(hasTestTag("statistics_selected_variation"))
        rule.onNodeWithTag("statistics_selected_variation", useUnmergedTree = true).assertTextEquals("Вариация: ${narrow.name}")
        rule.onNodeWithTag("statistics_list", useUnmergedTree = true).performScrollToNode(hasTestTag("statistics_record_variation"))
        rule.onNodeWithTag("statistics_record_variation", useUnmergedTree = true).assertTextEquals("Вариация: ${narrow.name}")
        capture("variation-cards")
        rule.onNodeWithTag("statistics_list").performScrollToIndex(0)
        rule.onNodeWithText("Частота").performClick()
        Assert.assertEquals(setOf(narrow.id), state.activeVariationIds)
        rule.onNodeWithTag("statistics_variation_dropdown").assertExists()
        rule.onNodeWithText("Все тренировки").performClick()
        rule.onNodeWithTag("statistics_variation_dropdown").assertDoesNotExist()
        Assert.assertEquals(FrequencyTotals(1, 2), state.frequency.allTime)
        rule.onNodeWithText("По упражнению").performClick()
        Assert.assertEquals(setOf(narrow.id), state.activeVariationIds)
        Assert.assertEquals(FrequencyTotals(1, 1), state.frequency.allTime)
    }

    @Test fun historyShowsAllVariationsAndCanFilterTheCurrentOrdinaryApproaches() {
        rule.setContent {
            TrainingStatsTheme {
                CompositionLocalProvider(LocalDateFormat provides rememberDateTimeFormatter()) {
                    BottomSheetHost {
                        val sheet = rememberBottomSheetState()
                        TextButton(onClick = { sheet.show(ExerciseHistoryBottomSheetArgs.buildArgs(pull.id)) }) { Text("Открыть историю") }
                        ExerciseHistoryBottomSheet(sheet)
                    }
                }
            }
        }
        rule.onNodeWithText("Открыть историю").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithText(narrow.name).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText(narrow.name).assertExists()
        capture("variation-history")
        rule.onNodeWithText("Только текущая вариация").performClick()
        rule.onNodeWithText(narrow.name).assertDoesNotExist()
        rule.onNodeWithText("Только текущая вариация").performClick()
        rule.onNodeWithText(narrow.name).assertExists()
    }

    @Test fun workoutSelectionChangesEveryApproachAndNewSetsInheritItAfterReopening() {
        rule.setContent { SharedApp() }
        rule.waitUntil(5_000) { rule.onAllNodesWithText(pull.name, substring = true).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText(pull.name, substring = true).performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithContentDescription("Повторения").fetchSemanticsNodes().isNotEmpty() }
        rule.onAllNodesWithText(pull.name)[0].performClick()
        rule.onNodeWithText("+ Добавить вариацию").performClick()
        rule.onNode(hasSetTextAction()).performTextInput("Обычный")
        rule.onNodeWithText("Создать").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithText("Вариации").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithText("Обычный").assertExists()
        rule.onAllNodesWithContentDescription("Повторения")[1].performTextInput("8")
        rule.onNodeWithText("Обновить").performScrollTo().performClick()
        rule.waitUntil(5_000) { runBlocking { RoomWorkoutDatasource(db).getWorkoutById(workout.id).steps.size == 3 } }
        runBlocking {
            val saved = RoomWorkoutDatasource(db).getWorkoutById(workout.id)
            val created = exercises.getExercise(pull.id).variations.single { it.name == "Обычный" }
            Assert.assertEquals(listOf(created.id, created.id, narrow.id), saved.steps.map { it.variationId })
        }
        rule.onNodeWithText(pull.name, substring = true).performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithText("Обычный").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Обычный").assertExists()
    }

    @Test fun selectorHighlightsTheCurrentIdAndRestoresTheOrdinarySelection() {
        rule.setContent {
            TrainingStatsTheme {
                BottomSheetHost {
                    var selectedId by remember { mutableStateOf<String?>(narrow.id) }
                    val sheet = rememberBottomSheetState()
                    TextButton(onClick = {
                        sheet.show(mapOf("groupId" to 5L, "exerciseId" to pull.id) +
                            selectedId?.let { mapOf("variationId" to it) }.orEmpty())
                    }) { Text("Выбрать вариацию") }
                    VariationSelectorBottomSheet(sheet) { _, variation -> selectedId = variation?.id }
                }
            }
        }
        rule.onNodeWithText("Выбрать вариацию").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("variation_option_narrow").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("variation_option_narrow").assertIsSelected()
        rule.onNodeWithTag("variation_option_wide").assertIsNotSelected()
        rule.onNodeWithTag("variation_option_ordinary").assertIsNotSelected()
        capture("variation-selector")
        rule.onNodeWithTag("variation_option_ordinary").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithText("Выбрать вариацию").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Выбрать вариацию").performClick()
        rule.onNodeWithTag("variation_option_ordinary").assertIsSelected()
        rule.onNodeWithTag("variation_option_narrow").assertIsNotSelected()
    }

    @Test fun addingTheFirstVariationShowsAnOrdinaryOptionThatIsNotSavedAsANamedVariation() {
        rule.setContent {
            TrainingStatsTheme {
                BottomSheetHost {
                    val sheet = rememberBottomSheetState()
                    TextButton(onClick = { sheet.show() }) { Text("Новое упражнение") }
                    ExerciseEditorBottomSheet(sheet)
                }
            }
        }
        rule.onNodeWithText("Новое упражнение").performClick()
        rule.onNodeWithTag("exercise_editor_ordinary_variation").assertDoesNotExist()
        rule.onNode(hasText("название") and hasSetTextAction()).performTextInput("Новое упражнение 45°")
        rule.onNodeWithText("+ Добавить вариацию").performScrollTo().performClick()
        rule.onNodeWithTag("exercise_editor_ordinary_variation").assertExists()
        rule.onNodeWithTag("variation_names_error").assertDoesNotExist()
        val ordinaryHeight = rule.onNodeWithTag("exercise_editor_ordinary_variation").fetchSemanticsNode().boundsInRoot.height
        val inputHeight = rule.onNode(hasText("Название вариации") and hasSetTextAction()).fetchSemanticsNode().boundsInRoot.height
        Assert.assertEquals(ordinaryHeight, inputHeight, 1f)
        rule.onAllNodes(hasSetTextAction() and hasAnyAncestor(hasTestTag("exercise_editor_ordinary_variation"))).assertCountEquals(0)
        rule.onNode(hasText("Название вариации") and hasSetTextAction()).performTextInput("Ручка 45°")
        capture("variation-editor")
        rule.onNodeWithText("Сохранить").performScrollTo().performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithText("Новое упражнение").fetchSemanticsNodes().isNotEmpty() }
        runBlocking {
            val created = exercises.getAllExercises().single { it.name == "новое упражнение 45°" }
            Assert.assertEquals(listOf("Ручка 45°"), created.variations.map { it.name })
        }
    }

    @Test fun defaultChoiceIsSavedAndOrdinaryCanBeRestored() {
        rule.setContent {
            TrainingStatsTheme {
                BottomSheetHost {
                    val sheet = rememberBottomSheetState()
                    TextButton(onClick = { sheet.show(ExerciseEditorBottomSheetArgs.buildArgs(pull.id)) }) { Text("Открыть редактор") }
                    ExerciseEditorBottomSheet(sheet)
                }
            }
        }
        rule.onNodeWithText("Открыть редактор").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("default_variation_narrow").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("default_variation_ordinary").assertIsSelected()
        rule.onNodeWithTag("default_variation_narrow").performClick().assertIsSelected()
        rule.onNodeWithTag("default_variation_wide").performClick().assertIsSelected()
        rule.onNodeWithTag("default_variation_narrow").assertIsNotSelected()
        capture("variation-default-editor")
        rule.onNodeWithText("Сохранить").performScrollTo().performClick()
        rule.waitUntil(5_000) { runBlocking { exercises.getExercise(pull.id).variations.any { it.id == wide.id && it.isDefault } } }
        rule.waitUntil(5_000) { rule.onAllNodesWithText("Открыть редактор").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Открыть редактор").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("default_variation_wide").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("default_variation_wide").assertIsSelected()
        rule.onNodeWithTag("default_variation_ordinary").performClick().assertIsSelected()
        rule.onNodeWithText("Сохранить").performScrollTo().performClick()
        rule.waitUntil(5_000) { runBlocking { exercises.getExercise(pull.id).variations.none { it.isDefault } } }
    }

    @Test fun exerciseSelectorAddsTheDefaultAndExplicitChipsInOneClick() {
        runBlocking { exercises.updateExercise(pull.copy(variations = listOf(narrow.copy(isDefault = true), wide))) }
        val selected = mutableListOf<String?>()
        rule.setContent {
            TrainingStatsTheme {
                CompositionLocalProvider(LocalDateFormat provides rememberDateTimeFormatter()) {
                    BottomSheetHost {
                        val viewModel = koinViewModel<ExerciseListViewModel>()
                        val sheet = rememberBottomSheetState()
                        viewModel.LaunchCollectEffects {
                            if (it is com.danilkha.trainstats.features.exercises.ui.ExerciseListSideEffect.ExerciseClicked) {
                                selected.add(it.variation?.id)
                                sheet.hide()
                            }
                        }
                        TextButton(onClick = { sheet.show() }) { Text("Добавить упражнение") }
                        ExerciseSelectorBottomSheet(sheet, openExerciseEditor = {})
                    }
                }
            }
        }
        rule.onNodeWithText("Добавить упражнение").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("exercise_card_pull").fetchSemanticsNodes().isNotEmpty() }
        capture("exercise-variation-chips")
        rule.onNodeWithTag("exercise_card_pull").performClick()
        rule.waitUntil(5_000) { selected.size == 1 }
        Assert.assertEquals(narrow.id, selected.last())
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("exercise_card_pull").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithText("Добавить упражнение").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("exercise_variation_pull_ordinary").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("exercise_variation_pull_ordinary").performClick()
        rule.waitUntil(5_000) { selected.size == 2 }
        Assert.assertNull(selected.last())
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("exercise_card_pull").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithText("Добавить упражнение").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("exercise_variations_pull", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("exercise_variations_pull", useUnmergedTree = true).performScrollToNode(hasTestTag("exercise_variation_pull_wide"))
        rule.onNodeWithTag("exercise_variation_pull_wide").performClick()
        rule.waitUntil(5_000) { selected.size == 3 }
        Assert.assertEquals(wide.id, selected.last())
    }

    @Test fun addingAnExerciseFromItsCardUsesTheSavedDefaultInTheWorkout() {
        runBlocking { exercises.updateExercise(pull.copy(variations = listOf(narrow.copy(isDefault = true), wide))) }
        rule.setContent { SharedApp() }
        rule.waitUntil(5_000) { rule.onAllNodesWithText(pull.name, substring = true).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText(pull.name, substring = true).performClick()
        rule.onNodeWithText("Добавить упражнение").performScrollTo().performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("exercise_card_pull").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("exercise_card_pull").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithContentDescription("Повторения").fetchSemanticsNodes().size == 5 }
        rule.onAllNodesWithContentDescription("Повторения").onLast().performScrollTo().performTextInput("8")
        rule.onNodeWithText("Обновить").performScrollTo().performClick()
        rule.waitUntil(5_000) { runBlocking { RoomWorkoutDatasource(db).getWorkoutById(workout.id).steps.size == 3 } }
        runBlocking { Assert.assertEquals(narrow.id, RoomWorkoutDatasource(db).getWorkoutById(workout.id).steps.last().variationId) }
    }

    private fun capture(name: String) {
        rule.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(instrumentation.targetContext.cacheDir, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
