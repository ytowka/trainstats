package com.danilkha.trainstats.features.workout.ui.editor

import androidx.compose.runtime.Immutable
import com.danilkha.commoncore.utils.toLocal
import com.danilkha.trainstats.features.exercises.ui.ExerciseModel
import com.danilkha.trainstats.features.workout.domain.model.SetParams
import com.danilkha.trainstats.features.workout.domain.model.WorkoutParams
import com.danilkha.trainstats.features.workout.ui.ExerciseGroup
import com.danilkha.trainstats.features.workout.ui.ExerciseSetSlot
import com.danilkha.trainstats.features.workout.ui.Side
import com.danilkha.trainstats.features.workout.ui.WorkoutModel
import com.danilkha.trainstats.features.workout.ui.toDomain
import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Immutable
data class WorkoutState(
    val initialWorkout: WorkoutModel? = null,
    val date: LocalDate = Clock.System.now().toLocal().date,
    val groups: List<ExerciseGroup> = emptyList(),
    val collapsedGroupIds: Set<Long> = emptySet(),
    val pendingDelete: Set<Long> = emptySet(),
    val initialization: WorkoutEditorInitialization? = null,
    val lastEdited: Instant? = null,
){

    fun mapToParams() = WorkoutParams(
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
        },
        lastEdited = lastEdited
    )
}

enum class WorkoutEditorInitialization { NEW, EDIT }

sealed interface WorkoutEvent {
    data class RequestInit(val editingId: Long?) : WorkoutEvent
    data class InitState(val state: WorkoutState) : WorkoutEvent

    data class ChangeDate(val date: LocalDate) : WorkoutEvent

    data class ToggleGroup(val groupIndex: Int) : WorkoutEvent

    data class AddExercise(val exercise: ExerciseModel) : WorkoutEvent

    data class EditWeight(
        val groupIndex: Int,
        val setIndex: Int,
        val kg: Float?
    ) : WorkoutEvent

    data class EditReps(
        val groupIndex: Int,
        val setIndex: Int,
        val side: Side?,
        val reps: Float?
    ) : WorkoutEvent

    data class OnSetMove(
        val groupIndex: Int,
        val from: Int,
        val to: Int
    ) : WorkoutEvent

    data class OnGroupMove(
        val from: Int,
        val to: Int
    ) : WorkoutEvent

    data class DeleteSet(
        val groupIndex: Int,
        val setIndex: Int
    ) : WorkoutEvent

    data class CommitDeleteSet(
        val setId: Long
    ) : WorkoutEvent

    data class ReturnDeletedSet(
        val groupIndex: Int,
        val setIndex: Int
    ) : WorkoutEvent

    data class DeleteGroup(val groupIndex: Int) : WorkoutEvent

    object DeleteWorkout : WorkoutEvent

    object SaveWorkout : WorkoutEvent
    object UpdateDateTime : WorkoutEvent
}

sealed interface WorkoutSideEffect{

    object Deleted : WorkoutSideEffect
    object OpenExerciseSelector : WorkoutSideEffect
}
