package com.danilkha.trainstats

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.danilkha.trainstats.di.*
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.time.Instant

@OptIn(kotlin.time.ExperimentalTime::class)
@RunWith(AndroidJUnit4::class)
class StatisticsScreenTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val exercises = mockk<ExerciseRepository>()
    private val workouts = mockk<WorkoutRepository>()
    private val press = ExerciseData("press", "Жим лёжа", null, false, true)
    private val pullup = ExerciseData("pullup", "Подтягивания", null, false, false)
    private val workout = Workout(
        id = "test-workout",
        dateTime = Instant.parse("2026-10-09T09:00:00Z"),
        steps = listOf(
            ExerciseSet("set-1", "test-workout", press, Repetitions.Single(10f), Kg(40f), 0),
            ExerciseSet("set-2", "test-workout", press, Repetitions.Single(8f), Kg(45f), 1),
        ),
        saved = false, archived = false,
    )
    private lateinit var restoration: StateRestorationTester
    private val chartDescription = "График прогресса: даты по горизонтали, выбранный показатель по вертикали"

    @Before
    fun setup() {
        stopKoin()
        coEvery { exercises.getAllExercises() } returns listOf(press, pullup)
        coEvery { workouts.getAll() } returns listOf(workout)
        coEvery { workouts.getWorkoutById(workout.id) } returns workout
        every { workouts.getWorkoutHistory() } returns flowOf(listOf(
            WorkoutPreview(workout.id, workout.dateTime, listOf(press.name), saved = false, archived = false),
        ))
        val context = ApplicationProvider.getApplicationContext<Context>()
        startKoin {
            androidContext(context)
            modules(platformModule, dataModule, repositoryModule, useCaseModule, androidSharedModule, viewModelModule,
                module {
                    single<ExerciseRepository> { exercises }
                    single<WorkoutRepository> { workouts }
                },
            )
        }
        restoration = StateRestorationTester(rule)
        restoration.setContent { SharedApp() }
        rule.onNodeWithContentDescription("График").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithText("Показатель").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithContentDescription(chartDescription).assertIsDisplayed()
    }

    @After
    fun teardown() { stopKoin() }

    @Test
    fun searchFiltersExercisesAndBodyweightShowsEmptyHistory() {
        rule.onNodeWithText(press.name).performClick()
        rule.onNode(hasSetTextAction()).performTextInput("пОдТ")
        rule.onAllNodesWithText(press.name).assertCountEquals(1)
        rule.onNodeWithText(pullup.name).performClick()
        rule.onNodeWithText("Повторения").assertExists()
        rule.onNodeWithText("Пока нет тренировок с этим упражнением.").assertExists()
    }

    @Test
    fun dropdownsUseTheWholeCardWidthAndPaddingIsClickable() {
        val cardWidth = rule.onNodeWithTag("statistics_filters").fetchSemanticsNode().boundsInRoot.width
        rule.onNodeWithTag("statistics_exercise_dropdown").performTouchInput { click(Offset(centerX, 4 * rule.density.density)) }
        assertEquals(cardWidth, rule.onNodeWithTag("statistics_exercise_menu").fetchSemanticsNode().boundsInRoot.width, 1f)
        rule.onNodeWithText(pullup.name).performClick()
        rule.onNodeWithTag("statistics_metric_dropdown").performTouchInput { click(Offset(4 * rule.density.density, centerY)) }
        assertEquals(cardWidth, rule.onNodeWithTag("statistics_metric_menu").fetchSemanticsNode().boundsInRoot.width, 1f)
        rule.onNodeWithText("Объём подхода").performClick()
    }

    @Test
    fun hiddenSetsAreExcludedFromPointSelection() {
        rule.onNodeWithText("Подход 2").performScrollTo().performClick().assertIsOff()
        rule.onNodeWithText("Последний подход").performScrollTo().performClick()
        rule.onNodeWithText("Подход 1 · 40 кг").assertExists()
        rule.onNodeWithText("Подход 2").performScrollTo().performClick().assertIsOn()
        rule.onNodeWithText("Последний подход").performScrollTo().performClick()
        rule.onNodeWithText("Подход 2 · 45 кг").assertExists()
    }

    @Test
    fun zoomAndMetricSelectionWork() {
        val chart = rule.onNodeWithContentDescription(chartDescription)
        chart.performScrollTo().performTouchInput {
            pinch(center - Offset(35f, 0f), center + Offset(35f, 0f), center - Offset(120f, 0f), center + Offset(120f, 0f))
        }
        rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress)).assertExists()
        chart.performTouchInput { swipeLeft() }
        rule.onNodeWithText("Весь период").performScrollTo().performClick()
        rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress)).assertDoesNotExist()
        rule.onNodeWithText("+").performScrollTo().performClick()
        rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress)).assertExists()
        rule.onNodeWithText("Весь период").performClick()
        rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress)).assertDoesNotExist()
        rule.onNodeWithText("Вес", useUnmergedTree = true).performScrollTo().performClick()
        rule.onNodeWithText("Объём подхода").performClick()
        rule.onNodeWithText("Последний подход").performScrollTo().performClick()
        rule.onNodeWithText("Подход 2 · 360 кг·повт").assertExists()
    }

    @Test
    fun pointTapOpensReadOnlyWorkoutAndNavigationRestores() {
        val chart = rule.onNodeWithContentDescription(chartDescription)
        chart.performScrollTo()
        chart.performTouchInput {
            val top = 12 * rule.density.density
            val bottom = height - 44 * rule.density.density
            // The newest workout appears near the end of the initial six-month window.
            val right = width - 12 * rule.density.density
            click(Offset(right - 8 * rule.density.density, bottom - 45f / 49.5f * (bottom - top)))
        }
        rule.waitUntil(3_000) { rule.onAllNodesWithText("Подход 2 · 45 кг").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Подход 2 · 45 кг").assertExists()
        rule.onNode(hasClickAction() and hasText("•", substring = true)).performClick()
        rule.onNodeWithText("Просмотр тренировки").assertExists()
        rule.onNodeWithText("45 кг").assertExists()
        rule.onNodeWithText("8 повт").assertExists()
        rule.onNodeWithText("Подход 1").assertDoesNotExist()
        rule.onNodeWithText("Подход 2").assertDoesNotExist()
        rule.onNodeWithText("Сохранить").assertDoesNotExist()
        rule.onNode(hasSetTextAction()).assertDoesNotExist()
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithText("Просмотр тренировки").assertExists()
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.onNodeWithText("Статистика").assertExists()
        rule.onNodeWithText("Подход 2 · 45 кг").assertExists()
        coVerify(exactly = 0) { workouts.saveWorkout(any()) }
        coVerify(exactly = 0) { workouts.commitWorkoutSave(any()) }
    }
}
