package com.danilkha.trainstats.features.workout.ui

import androidx.compose.runtime.Immutable

@Immutable
data class WorkoutState(
    val initialWorkout: WorkoutModel? = null,
    val groups: List<ExerciseGroup> = emptyList(),
    val collapsedGroupIds: Set<Long> = emptySet(),
)

sealed interface WorkoutSideEffect{

}
