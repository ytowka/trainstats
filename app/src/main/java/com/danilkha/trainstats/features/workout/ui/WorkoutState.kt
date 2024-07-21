package com.danilkha.trainstats.features.workout.ui

import androidx.compose.runtime.Immutable
import korlibs.time.Date
import korlibs.time.DateTime

@Immutable
data class WorkoutState(
    val initialWorkout: WorkoutModel? = null,
    val date: Date = DateTime.now().date,
    val groups: List<ExerciseGroup> = emptyList(),
    val collapsedGroupIds: Set<Long> = emptySet(),
    val pendingDelete: Set<Long> = emptySet(),
)

sealed interface WorkoutSideEffect{

}
