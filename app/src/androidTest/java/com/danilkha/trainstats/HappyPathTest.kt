package com.danilkha.trainstats

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.danilkha.commonds.theme.TrainingStatsTheme
import com.danilkha.trainstats.core.utils.JvmDateTimeFormatter
import com.danilkha.trainstats.core.utils.LocalDateFormat
import com.danilkha.trainstats.di.androidSharedModule
import com.danilkha.trainstats.di.viewModelModule
import com.danilkha.trainstats.di.dataModule
import com.danilkha.trainstats.di.platformModule
import com.danilkha.trainstats.di.repositoryModule
import com.danilkha.trainstats.di.useCaseModule
import com.danilkha.trainstats.entrypoint.db.TrainStatsDb
import com.danilkha.trainstats.features.navigation.RootScreen
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class HappyPathTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val testDbModule = module {
        single<TrainStatsDb> {
            Room.inMemoryDatabaseBuilder(
                get<Context>(),
                TrainStatsDb::class.java
            ).allowMainThreadQueries().build()
        }
    }

    @Before
    fun setup() {
        stopKoin()
        val context = ApplicationProvider.getApplicationContext<Context>()
        startKoin {
            androidContext(context)
            modules(
                platformModule,
                dataModule,
                repositoryModule,
                useCaseModule,
                androidSharedModule,
                viewModelModule,
                testDbModule
            )
        }
        composeTestRule.setContent {
            val context = LocalContext.current
            val dateFormat = JvmDateTimeFormatter(context)
            TrainingStatsTheme {
                CompositionLocalProvider(LocalDateFormat provides dateFormat) {
                    RootScreen()
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @After
    fun teardown() {
        stopKoin()
    }

    // region — Helpers

    private fun goToExercisesTab() {
        composeTestRule.onNodeWithContentDescription("Упражнения", substring = true).performClick()
        composeTestRule.waitForIdle()
    }

    private fun goToWorkoutsTab() {
        composeTestRule.onNodeWithContentDescription("Тренировки", substring = true).performClick()
        composeTestRule.waitForIdle()
    }

    private fun findTextUnmerged(text: String): Boolean {
        return composeTestRule.onAllNodes(hasText(text), useUnmergedTree = true)
            .fetchSemanticsNodes().isNotEmpty()
    }

    private fun createExerciseViaUi(rawName: String) {
        goToExercisesTab()

        composeTestRule.onNodeWithContentDescription("Новое упражнение").performClick()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Сохранить")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onAllNodes(hasSetTextAction())[1].performTextInput(rawName)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Сохранить").performClick()

        val normalizedName = rawName.lowercase().trim()
        composeTestRule.waitUntil(5_000) {
            findTextUnmerged(normalizedName)
        }
    }

    private fun createWorkoutViaUi(exerciseNormalizedName: String, weight: String, reps: String) {
        goToWorkoutsTab()

        composeTestRule.onNodeWithContentDescription("Новая тренировка").performClick()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Сохранить")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.waitUntil(15_000) {
            composeTestRule.onAllNodesWithText("Добавить упражнение")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.waitUntil(10_000) {
            findTextUnmerged(exerciseNormalizedName)
        }

        composeTestRule.onAllNodes(hasText(exerciseNormalizedName), useUnmergedTree = true)
            .get(0).performClick()

        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithContentDescription("Вес")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithContentDescription("Вес").performTextInput(weight)
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithContentDescription("Повторения")[0].performTextInput(reps)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Сохранить").performClick()

        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText("История")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    // endregion

    // region — Scenario 1: Exercise creation

    @Test
    fun scenario1_exerciseCreation() {
        createExerciseViaUi("Test Exercise QA")

        composeTestRule.waitUntil(5_000) {
            findTextUnmerged("test exercise qa")
        }
    }

    // endregion

    // region — Scenario 2: Workout creation

    @Test
    fun scenario2_workoutCreation() {
        createExerciseViaUi("Test Exercise QA")

        createWorkoutViaUi("test exercise qa", "60", "10")

        composeTestRule.waitUntil(10_000) {
            findTextUnmerged("test exercise qa")
        }
    }

    // endregion

    // region — Scenario 3: Exercise history

    @Test
    fun scenario3_exerciseHistory() {
        createExerciseViaUi("Test Exercise QA")
        createWorkoutViaUi("test exercise qa", "60", "10")



        composeTestRule.onAllNodes(hasText("test exercise qa"), useUnmergedTree = true)
            .get(0).performClick()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithContentDescription("История упражнения")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithContentDescription("История упражнения").performClick()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Всего записей", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onAllNodesWithText("60").fetchSemanticsNodes().also {
            assert(it.isNotEmpty()) { "Expected weight '60' in history sheet" }
        }

        composeTestRule.onAllNodesWithText("10").fetchSemanticsNodes().also {
            assert(it.isNotEmpty()) { "Expected reps '10' in history sheet" }
        }
    }

    // endregion
}
