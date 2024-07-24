package com.danilkha.trainstats.features.workout.ui.editor

import androidx.compose.runtime.Immutable
import com.danilkha.trainstats.features.workout.ui.ExerciseGroup
import com.danilkha.trainstats.features.workout.ui.WorkoutModel
import korlibs.time.Date
import korlibs.time.DateTime

@Immutable
data class WorkoutState(
    val initialWorkout: WorkoutModel? = null,
    val initialized: Boolean = false,
    val date: Date = DateTime.now().date,
    val groups: List<ExerciseGroup> = emptyList(),
    val collapsedGroupIds: Set<Long> = emptySet(),
    val pendingDelete: Set<Long> = emptySet(),
)

sealed interface WorkoutSideEffect{

    object Deleted : WorkoutSideEffect
}
