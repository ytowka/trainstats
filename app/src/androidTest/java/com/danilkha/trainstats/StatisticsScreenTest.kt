package com.danilkha.trainstats

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
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
import org.junit.Assert.assertTrue
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
    private val separated = ExerciseData("separated", "Раздельное упражнение", null, true, true)
    private val bodyweight = ExerciseData("bodyweight", "Отжимания", null, false, false)
    private val extraExercises = (1..30).map { ExerciseData("extra-$it", "Упражнение $it", null, false, true) }
    private val workout = Workout(
        id = "test-workout",
        dateTime = Instant.parse("2026-10-09T09:00:00Z"),
        steps = listOf(
            ExerciseSet("set-1", "test-workout", press, Repetitions.Single(10f), Kg(40f), 0),
            ExerciseSet("set-2", "test-workout", press, Repetitions.Single(8f), Kg(45f), 1),
            ExerciseSet("set-3", "test-workout", separated, Repetitions.Double(8f, 12f), Kg(20f), 2),
            ExerciseSet("set-4", "test-workout", bodyweight, Repetitions.Single(15f), null, 3),
        ),
        saved = false, archived = false,
    )
    private lateinit var restoration: StateRestorationTester
    private val chartDescription = "График прогресса: даты по горизонтали, выбранный показатель по вертикали"

    @Before
    fun setup() {
        stopKoin()
        coEvery { exercises.getAllExercises() } returns listOf(press, pullup, separated, bodyweight) + extraExercises
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

    private fun scrollTo(matcher: SemanticsMatcher, useUnmergedTree: Boolean = false): SemanticsNodeInteraction {
        rule.onNode(hasScrollToIndexAction(), useUnmergedTree).performScrollToNode(matcher)
        return rule.onNode(matcher, useUnmergedTree)
    }

    @After
    fun teardown() { stopKoin() }

    @Test
    fun searchFiltersExercisesAndBodyweightShowsEmptyHistory() {
        rule.onNodeWithTag("statistics_exercise_dropdown").performClick()
        rule.onNode(hasSetTextAction()).performTextInput("пОдТ")
        rule.onNode(hasText(press.name) and hasAnyAncestor(hasTestTag("statistics_exercise_menu"))).assertDoesNotExist()
        rule.onNodeWithText(pullup.name).performClick()
        rule.onNodeWithText("Повторения").assertExists()
        rule.onNodeWithText("Пока нет тренировок с этим упражнением.").assertExists()
    }

    @Test
    fun searchStaysPinnedWhileExerciseMenuScrollsAndReturnsResultsToTop() {
        rule.onNodeWithTag("statistics_exercise_dropdown").performClick()
        val search = rule.onNodeWithTag("statistics_exercise_search")
        val menu = rule.onNodeWithTag("statistics_exercise_menu")
        val searchBounds = search.fetchSemanticsNode().boundsInRoot
        menu.performTouchInput {
            swipe(Offset(centerX, height * .9f), Offset(centerX, height * .4f))
        }
        search.assertIsDisplayed()
        assertEquals(searchBounds, search.fetchSemanticsNode().boundsInRoot)
        rule.onNode(hasText(press.name) and hasAnyAncestor(hasTestTag("statistics_exercise_menu"))).assertIsNotDisplayed()
        search.performTouchInput { click() }
        search.performTextInput("Упражнение")
        val focusedBounds = search.fetchSemanticsNode().boundsInRoot
        menu.performTouchInput {
            swipe(Offset(centerX, height * .9f), Offset(centerX, height * .4f))
        }
        search.assertIsDisplayed()
        assertEquals(focusedBounds, search.fetchSemanticsNode().boundsInRoot)
        search.performTextClearance()
        search.performTextInput("Упражнение 30")
        rule.onNode(hasText("Упражнение 30") and !hasSetTextAction()).assertIsDisplayed().performClick()
        rule.onNodeWithTag("statistics_exercise_dropdown").assertTextContains("Упражнение 30")
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
    fun maximumIsTheOnlyDefaultAndHiddenSetsAreExcludedFromSelection() {
        scrollTo(hasText("макс", ignoreCase = true)).assertIsOn()
        rule.onNode(hasText("Подход 1") and isToggleable()).assertIsOff()
        rule.onNode(hasText("Подход 2") and isToggleable()).assertIsOff()
        scrollTo(hasText("Последний подход")).performClick()
        rule.onNodeWithText("Подход 2 · 45 кг × 8 повт").assertExists()
        scrollTo(hasText("макс", ignoreCase = true)).performClick().assertIsOff()
        scrollTo(hasText("Последний подход")).assertIsNotEnabled()
        scrollTo(hasText("Подход 1") and isToggleable()).performClick().assertIsOn()
        scrollTo(hasText("Последний подход")).performClick()
        rule.onNodeWithText("Подход 1 · 40 кг × 10 повт").assertExists()
        scrollTo(hasText("Подход 2") and isToggleable()).performClick().assertIsOn()
        scrollTo(hasText("Последний подход")).performClick()
        rule.onNodeWithText("Подход 2 · 45 кг × 8 повт").assertExists()
    }

    @Test
    fun allTimeRecordRemainsVisibleWhenEverySeriesIsHiddenAndOpensWorkout() {
        scrollTo(hasText("макс", ignoreCase = true)).performClick()
        scrollTo(hasText("Рекорд за всё время")).assertIsDisplayed()
        rule.onNodeWithTag("statistics_record_value", useUnmergedTree = true).assertTextEquals("45 кг × 8 повт")
        scrollTo(hasTestTag("statistics_record_value"), useUnmergedTree = true).performTouchInput { click() }
        rule.onNodeWithText("Просмотр тренировки").assertExists()
        rule.onNodeWithText("45 кг").assertExists()
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        scrollTo(hasTestTag("statistics_record_workout")).performTouchInput {
            click(Offset(centerX, height - 4 * rule.density.density))
        }
        rule.onNodeWithText("Просмотр тренировки").assertExists()
        coVerify(exactly = 0) { workouts.saveWorkout(any()) }
    }

    @Test
    fun zoomAndMetricSelectionWork() {
        val chart = rule.onNodeWithContentDescription(chartDescription)
        chart.performScrollTo().performTouchInput {
            pinch(center - Offset(35f, 0f), center + Offset(35f, 0f), center - Offset(120f, 0f), center + Offset(120f, 0f))
        }
        val slider = rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress))
        slider.assertExists()
        val periodBefore = slider.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current
        val chartTop = chart.fetchSemanticsNode().boundsInRoot.top
        chart.performTouchInput { swipeLeft() }
        val periodAfter = slider.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
        assertTrue(periodAfter.current > periodBefore)
        assertEquals(chartTop, chart.fetchSemanticsNode().boundsInRoot.top, 1f)
        chart.performTouchInput { swipeUp() }
        assertTrue(chart.fetchSemanticsNode().boundsInRoot.top < chartTop - 40 * rule.density.density)
        assertEquals(periodAfter, slider.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo])
        chart.performScrollTo().performTouchInput { doubleClick() }
        assertEquals(0f..0f, slider.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].range)
        scrollTo(hasTestTag("statistics_metric_dropdown")).performClick()
        rule.onNodeWithText("Объём подхода").performClick()
        scrollTo(hasText("Последний подход")).performClick()
        rule.onNodeWithText("Подход 1 · 40 кг × 10 повт").assertExists()
    }

    @Test
    fun verticalDragFromChartScrollsPageWithoutChangingScaleOrSelectingPoint() {
        val chart = rule.onNodeWithContentDescription(chartDescription)
        val slider = rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress))
        val periodBefore = slider.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
        val topBefore = chart.fetchSemanticsNode().boundsInRoot.top
        chart.performTouchInput {
            swipe(Offset(centerX, height * .8f), Offset(centerX + 10f, height * .25f))
        }
        assertTrue(chart.fetchSemanticsNode().boundsInRoot.top < topBefore - 40 * rule.density.density)
        assertEquals(periodBefore, slider.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo])
        rule.onNodeWithTag("statistics_selected_point", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun horizontalDirectionStaysLockedWhenDragTurnsVertical() {
        val chart = rule.onNodeWithContentDescription(chartDescription)
        chart.performScrollTo()
        val topBefore = chart.fetchSemanticsNode().boundsInRoot.top
        chart.performTouchInput {
            down(center)
            moveBy(Offset(-80f, 0f), delayMillis = 100)
            moveBy(Offset(-10f, -100f), delayMillis = 100)
            up()
        }
        assertEquals(topBefore, chart.fetchSemanticsNode().boundsInRoot.top, 1f)
        rule.onNodeWithTag("statistics_selected_point", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun selectedAndRecordShowWholeApproachesForEveryMetric() {
        listOf("Вес" to (45 to 8), "Повторения" to (40 to 10), "Объём подхода" to (40 to 10)).forEach { (metric, values) ->
            scrollTo(hasTestTag("statistics_metric_dropdown")).performClick()
            rule.onNode(hasText(metric) and hasAnyAncestor(hasTestTag("statistics_metric_menu"))).performClick()
            scrollTo(hasText("Последний подход")).performClick()
            val number = if (values.first == 45) 2 else 1
            scrollTo(hasTestTag("statistics_selected_point"), useUnmergedTree = true)
                .assertTextEquals("Подход $number · ${values.first} кг × ${values.second} повт")
            scrollTo(hasTestTag("statistics_record_value"), useUnmergedTree = true)
                .assertTextEquals("${values.first} кг × ${values.second} повт")
            if (metric == "Объём подхода") rule.onNodeWithText("Подход 1 · 400 кг·повт").assertExists()
        }
    }

    @Test
    fun wholeApproachKeepsEachSideAndSupportsBodyweight() {
        listOf(
            separated to "20 кг × Л: 8 · П: 12 повт",
            bodyweight to "15 повт",
        ).forEach { (exercise, value) ->
            scrollTo(hasTestTag("statistics_exercise_dropdown")).performClick()
            rule.onNode(hasText(exercise.name) and hasAnyAncestor(hasTestTag("statistics_exercise_menu"))).performClick()
            scrollTo(hasText("Последний подход")).performClick()
            scrollTo(hasTestTag("statistics_selected_point"), useUnmergedTree = true).assertTextEquals("Подход 1 · $value")
            scrollTo(hasTestTag("statistics_record_value"), useUnmergedTree = true).assertTextEquals(value)
        }
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
        scrollTo(hasText("Последний подход"))
        rule.waitUntil(3_000) { rule.onAllNodesWithText("Подход 2 · 45 кг × 8 повт").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Подход 2 · 45 кг × 8 повт").assertExists()
        scrollTo(hasTestTag("statistics_selected_point"), useUnmergedTree = true).performTouchInput { click() }
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
        rule.onNodeWithText("Подход 2 · 45 кг × 8 повт").assertExists()
        scrollTo(hasTestTag("statistics_selected_workout")).performTouchInput {
            click(Offset(centerX, height - 4 * rule.density.density))
        }
        rule.onNodeWithText("Просмотр тренировки").assertExists()
        coVerify(exactly = 0) { workouts.saveWorkout(any()) }
        coVerify(exactly = 0) { workouts.commitWorkoutSave(any()) }
    }
}
