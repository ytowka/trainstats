package com.danilkha.trainstats.features.stats.domain

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.workout.domain.model.Repetitions
import com.danilkha.trainstats.features.workout.domain.model.Workout
import kotlin.time.Instant

enum class StatisticsMetric {
    Weight, Repetitions, Volume;

    fun value(point: StatisticsPoint): Float? = when (this) {
        Weight -> point.weight
        Repetitions -> point.repetitions
        Volume -> point.volume
    }
}

data class StatisticsPoint(
    val workoutId: String,
    val date: Instant,
    val setNumber: Int,
    val weight: Float?,
    val repetitions: Float,
    val volume: Float?,
)

data class StatisticsData(
    val exercises: List<ExerciseData>,
    val workouts: List<Workout>,
)

/** Set numbers belong to the selected exercise, even if it occurs in several groups. */
fun exerciseStatistics(workouts: List<Workout>, exerciseId: String): List<StatisticsPoint> =
    workouts.asSequence()
        // The editor persists history with saved=false; this flag is not a history filter.
        .filter { !it.archived }
        .sortedWith(compareBy<Workout> { it.dateTime }.thenBy { it.id })
        .flatMap { workout ->
            workout.steps.sortedBy { it.orderPosition }
                .filter { it.exerciseData.id == exerciseId }
                .mapIndexed { index, set ->
                    val totalReps = when (val reps = set.reps) {
                        is Repetitions.Single -> reps.reps
                        is Repetitions.Double -> reps.left + reps.right
                    }
                    val repetitions = when (set.reps) {
                        is Repetitions.Single -> totalReps
                        is Repetitions.Double -> totalReps / 2f
                    }
                    StatisticsPoint(
                        workoutId = workout.id,
                        date = workout.dateTime,
                        setNumber = index + 1,
                        weight = set.weight?.value,
                        repetitions = repetitions,
                        volume = set.weight?.value?.times(totalReps),
                    )
                }
        }.toList()
