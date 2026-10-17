package com.danilkha.trainstats.features.stats.domain

import com.danilkha.trainstats.features.workout.domain.model.Repetitions
import com.danilkha.trainstats.features.workout.domain.model.Workout
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

enum class FrequencyInterval {
    Week, Month, Quarter, Year;

    fun start(date: LocalDate): LocalDate = when (this) {
        Week -> date.minus(DatePeriod(days = date.dayOfWeek.ordinal))
        Month -> LocalDate(date.year, date.month, 1)
        Quarter -> LocalDate(date.year, ((date.month.ordinal / 3) * 3) + 1, 1)
        Year -> LocalDate(date.year, 1, 1)
    }

    fun next(start: LocalDate): LocalDate = start.plus(when (this) {
        Week -> DatePeriod(days = 7)
        Month -> DatePeriod(months = 1)
        Quarter -> DatePeriod(months = 3)
        Year -> DatePeriod(years = 1)
    })
}

enum class FrequencyScope { Exercise, AllWorkouts }
enum class FrequencyMetric { Workouts, Sets }

data class FrequencyTotals(val workouts: Int = 0, val sets: Int = 0) {
    fun value(metric: FrequencyMetric): Int = when (metric) {
        FrequencyMetric.Workouts -> workouts
        FrequencyMetric.Sets -> sets
    }
}

data class FrequencyBucket(
    val start: LocalDate,
    val endExclusive: LocalDate,
    val totals: FrequencyTotals,
)

data class FrequencyStatistics(
    val buckets: List<FrequencyBucket>,
    val allTime: FrequencyTotals,
)

/** Each column is one whole calendar interval, including intervals with no activity. */
fun frequencyStatistics(
    workouts: List<Workout>,
    exerciseId: String?,
    scope: FrequencyScope,
    interval: FrequencyInterval,
    today: LocalDate,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    variationIds: Set<String?>? = null,
): FrequencyStatistics {
    val completed = workouts.asSequence().filter { !it.archived }.mapNotNull { workout ->
        val sets = workout.steps.count { set ->
            val repetitions = when (val reps = set.reps) {
                is Repetitions.Single -> reps.reps
                is Repetitions.Double -> reps.left + reps.right
            }
            (scope == FrequencyScope.AllWorkouts || (set.exerciseData.id == exerciseId && (variationIds == null || set.variationId in variationIds))) &&
                repetitions.isFinite() && repetitions > 0f
        }
        if (sets == 0) null else workout.dateTime.toLocalDateTime(timeZone).date to sets
    }.toList()
    val grouped = completed.groupBy { interval.start(it.first) }
    val current = interval.start(today)
    var start = minOf(grouped.keys.minOrNull() ?: current, current)
    val last = maxOf(grouped.keys.maxOrNull() ?: current, current)
    val buckets = buildList {
        while (start <= last) {
            val entries = grouped[start].orEmpty()
            val end = interval.next(start)
            add(FrequencyBucket(start, end, FrequencyTotals(entries.size, entries.sumOf { it.second })))
            start = end
        }
    }
    return FrequencyStatistics(buckets, FrequencyTotals(completed.size, completed.sumOf { it.second }))
}
