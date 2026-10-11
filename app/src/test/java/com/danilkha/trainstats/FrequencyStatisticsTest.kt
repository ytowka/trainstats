package com.danilkha.trainstats

import androidx.lifecycle.viewModelScope
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.stats.domain.*
import com.danilkha.trainstats.features.stats.ui.*
import com.danilkha.trainstats.features.workout.domain.model.*
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FrequencyStatisticsTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf
    val models = mutableListOf<StatisticsViewModel>()
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest {
        models.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }
    val press = ExerciseData("press", "Жим лёжа", null, separated = false, hasWeight = true)
    val pullup = ExerciseData("pullup", "Подтягивания", null, separated = false, hasWeight = false)
    val today = LocalDate(2026, 10, 11)
    fun set(number: Int, exercise: ExerciseData = press, reps: Repetitions = Repetitions.Single(10f)) =
        ExerciseSet("set-$number", "workout", exercise, reps, null, number)
    fun workout(id: String, date: String, sets: List<ExerciseSet> = listOf(set(0))) =
        Workout(id, Instant.parse(date), sets, saved = false, archived = false)
    fun statistics(workouts: List<Workout>, interval: FrequencyInterval = FrequencyInterval.Week,
        scope: FrequencyScope = FrequencyScope.Exercise, zone: TimeZone = TimeZone.UTC) =
        frequencyStatistics(workouts, press.id, scope, interval, today, zone)

    Given("multiple approaches, repeated exercises, empty approaches and archives") {
        val history = listOf(
            workout("mixed", "2026-10-05T10:00:00Z", listOf(set(0), set(1, pullup), set(2),
                set(3, reps = Repetitions.Single(0f)), set(4, reps = Repetitions.Double(0f, 6f)))),
            workout("pullup", "2026-10-06T10:00:00Z", listOf(set(0, pullup))),
            workout("empty", "2026-10-07T10:00:00Z", listOf(set(0, reps = Repetitions.Double(0f, 0f)))),
            workout("archived", "2026-10-08T10:00:00Z").copy(archived = true),
            workout("invalid", "2026-10-09T10:00:00Z", listOf(set(0, reps = Repetitions.Single(Float.NaN)))),
        )
        Then("exercise mode counts each workout once and each completed approach once") {
            val data = statistics(history)
            data.allTime shouldBe FrequencyTotals(workouts = 1, sets = 3)
            data.buckets.single().totals shouldBe data.allTime
        }
        Then("all-workouts mode includes other exercises without double-counting workouts") {
            val data = statistics(history, scope = FrequencyScope.AllWorkouts)
            data.allTime shouldBe FrequencyTotals(workouts = 2, sets = 5)
            data.buckets.single().totals shouldBe data.allTime
        }
    }
    Given("history spanning three weeks with a gap") {
        val data = statistics(listOf(
            workout("earlier", "2026-09-21T10:00:00Z"),
            workout("monday", "2026-10-05T10:00:00Z"),
            workout("sunday", "2026-10-11T10:00:00Z", listOf(set(0), set(1))),
        ))
        Then("a week is one column, starts on Monday, and empty weeks stay visible") {
            data.buckets.map { it.start } shouldBe listOf(LocalDate(2026, 9, 21), LocalDate(2026, 9, 28), LocalDate(2026, 10, 5))
            data.buckets.map { it.totals } shouldBe listOf(FrequencyTotals(1, 1), FrequencyTotals(), FrequencyTotals(2, 3))
            data.buckets.last().endExclusive shouldBe LocalDate(2026, 10, 12)
            data.allTime shouldBe FrequencyTotals(3, 4)
        }
    }
    Given("workouts across month, quarter and year boundaries") {
        val history = listOf(
            workout("old", "2025-12-31T10:00:00Z"),
            workout("new", "2026-01-01T10:00:00Z"),
            workout("september", "2026-09-30T10:00:00Z"),
            workout("october", "2026-10-01T10:00:00Z"),
        )
        Then("month columns contain whole months rather than days") {
            val data = statistics(history, FrequencyInterval.Month)
            data.buckets.size shouldBe 11
            data.buckets.last().start shouldBe LocalDate(2026, 10, 1)
            data.buckets.last().endExclusive shouldBe LocalDate(2026, 11, 1)
            data.buckets.sumOf { it.totals.workouts } shouldBe 4
        }
        Then("quarter columns contain three whole months") {
            val data = statistics(history, FrequencyInterval.Quarter)
            data.buckets.size shouldBe 5
            data.buckets.map { it.totals.workouts } shouldBe listOf(1, 1, 0, 1, 1)
            data.buckets.last().endExclusive shouldBe LocalDate(2027, 1, 1)
        }
        Then("year columns contain whole years") {
            val data = statistics(history, FrequencyInterval.Year)
            data.buckets.size shouldBe 2
            data.buckets.map { it.totals.workouts } shouldBe listOf(1, 3)
        }
        Then("leap months and weeks crossing New Year have correct boundaries") {
            FrequencyInterval.Month.next(LocalDate(2024, 2, 1)) shouldBe LocalDate(2024, 3, 1)
            FrequencyInterval.Week.start(LocalDate(2026, 1, 1)) shouldBe LocalDate(2025, 12, 29)
            FrequencyInterval.Week.next(LocalDate(2025, 12, 29)) shouldBe LocalDate(2026, 1, 5)
        }
    }
    Given("a workout close to midnight at an interval boundary") {
        val data = statistics(listOf(workout("boundary", "2026-10-04T22:30:00Z")), zone = TimeZone.of("Europe/Moscow"))
        Then("grouping uses the user's local date") {
            data.buckets.single().start shouldBe LocalDate(2026, 10, 5)
            data.buckets.single().totals.workouts shouldBe 1
        }
    }
    Given("no matching completed history") {
        Then("the current interval remains selectable with zero totals") {
            val data = statistics(emptyList())
            data.buckets.single().start shouldBe LocalDate(2026, 10, 5)
            data.buckets.single().totals shouldBe FrequencyTotals()
            data.allTime shouldBe FrequencyTotals()
        }
    }
    Given("an exercise with old history and a selected metric point") {
        val vm = StatisticsViewModel(GetStatisticsUseCase(mockk(), mockk())).also { models.add(it) }
        val history = listOf(workout("old", "2026-09-22T10:00:00Z"), workout("recent", "2026-10-06T10:00:00Z"))
        val initial = StatisticsState(data = StatisticsData(listOf(press), history), exerciseId = press.id, frequencyToday = today)
        Then("column selection changes period totals while all-time totals remain intact") {
            initial.selectedFrequencyBucket.start shouldBe LocalDate(2026, 10, 5)
            val selected = vm.reduce(initial, StatisticsEvent.SelectFrequencyBucket(LocalDate(2026, 9, 28)))
            selected.selectedFrequencyBucket.totals shouldBe FrequencyTotals()
            selected.frequency.allTime shouldBe FrequencyTotals(2, 2)
            vm.reduce(selected, StatisticsEvent.SelectFrequencyInterval(FrequencyInterval.Month))
                .selectedFrequencyBucket.totals shouldBe FrequencyTotals(1, 1)
        }
        Then("tab and count switches preserve progress settings and period selection") {
            val progress = initial.copy(selectedPoint = initial.points.first(), visibleSeries = setOf(1), metric = StatisticsMetric.Repetitions)
            val frequency = vm.reduce(progress, StatisticsEvent.SelectTab(StatisticsTab.Frequency))
            val sets = vm.reduce(frequency, StatisticsEvent.SelectFrequencyMetric(FrequencyMetric.Sets))
            sets.frequencyMetric shouldBe FrequencyMetric.Sets
            val restored = vm.reduce(sets, StatisticsEvent.SelectTab(StatisticsTab.Metrics))
            restored.selectedPoint shouldBe progress.selectedPoint
            restored.metric shouldBe progress.metric
            restored.visibleSeries shouldBe progress.visibleSeries
        }
        Then("scope switches retain the exercise and show the current interval") {
            val selected = vm.reduce(initial, StatisticsEvent.SelectFrequencyBucket(LocalDate(2026, 9, 21)))
            val changed = vm.reduce(selected, StatisticsEvent.SelectFrequencyScope(FrequencyScope.AllWorkouts))
            changed.exerciseId shouldBe press.id
            changed.selectedFrequencyBucket.start shouldBe LocalDate(2026, 10, 5)
        }
    }
})
