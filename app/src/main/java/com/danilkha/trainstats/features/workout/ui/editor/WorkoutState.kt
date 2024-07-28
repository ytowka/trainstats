package com.danilkha.trainstats.features.workout.ui.editor

import androidx.compose.runtime.Immutable
import com.danilkha.trainstats.features.workout.domain.model.SetParams
import com.danilkha.trainstats.features.workout.domain.model.WorkoutParams
import com.danilkha.trainstats.features.workout.ui.ExerciseGroup
import com.danilkha.trainstats.features.workout.ui.ExerciseSetSlot
import com.danilkha.trainstats.features.workout.ui.WorkoutModel
import com.danilkha.trainstats.features.workout.ui.toDomain
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
){

    fun mapToParams() =  WorkoutParams(
        id = initialWorkout?.id,
        date = date,
        steps = groups.flatMap { group ->
            group.sets
                .filterIsInstance<ExerciseSetSlot.ExerciseSetModel>()
                .map { set ->
                group to set
            }
        }.map { (group, set) ->
            SetParams(
                exerciseId = group.exerciseId,
                reps = set.reps.toDomain(),
                weight = set.weight,
                exerciseName = group.name
            )
        }
    )
}

sealed interface WorkoutSideEffect{

    object Deleted : WorkoutSideEffect
}
