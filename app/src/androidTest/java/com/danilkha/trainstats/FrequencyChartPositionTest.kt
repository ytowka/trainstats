package com.danilkha.trainstats

import androidx.activity.ComponentActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.stats.domain.*
import com.danilkha.trainstats.features.stats.ui.*
import com.danilkha.trainstats.features.workout.domain.model.*
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
class FrequencyChartPositionTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun intervalChangesKeepRecentColumnsVisible() {
        val exercise = ExerciseData("press", "Жим лёжа", null, false, true)
        fun workout(id: String, date: String, count: Int) = Workout(
            id, Instant.parse(date), List(count) { index ->
                ExerciseSet("$id-$index", id, exercise, Repetitions.Single(10f), Kg(40f), index)
            }, saved = false, archived = false,
        )
        var state by mutableStateOf(StatisticsState(
            loading = false,
            data = StatisticsData(listOf(exercise), listOf(
                workout("old", "2010-01-05T12:00:00Z", 20),
                workout("recent", "2026-10-09T12:00:00Z", 1),
            )),
            exerciseId = exercise.id,
            tab = StatisticsTab.Frequency,
            frequencyToday = LocalDate(2026, 10, 11),
        ))
        rule.mainClock.autoAdvance = false
        rule.setContent {
            MaterialTheme {
                StatisticsPage(state, onEvent = { event ->
                    if (event is StatisticsEvent.SelectFrequencyInterval) {
                        state = state.copy(frequencyInterval = event.interval)
                    }
                }, onBack = {}, onWorkout = {})
            }
        }
        fun format(date: LocalDate) = "${date.day.toString().padStart(2, '0')}.${(date.month.ordinal + 1).toString().padStart(2, '0')}.${date.year}"
        fun description(bucket: FrequencyBucket) =
            "${format(bucket.start)} – ${format(bucket.endExclusive.minus(DatePeriod(days = 1)))}: Тренировки — ${bucket.totals.workouts}"

        for (interval in listOf(FrequencyInterval.Week, FrequencyInterval.Year, FrequencyInterval.Month, FrequencyInterval.Quarter, FrequencyInterval.Week)) {
            rule.runOnIdle { state = state.copy(frequencyInterval = interval) }
            rule.mainClock.advanceTimeByFrame()
            rule.onNodeWithContentDescription(description(state.selectedFrequencyBucket)).assertExists().assertIsSelected()
            rule.onNodeWithContentDescription(description(state.frequency.buckets.first())).assertDoesNotExist()
        }
    }
}
