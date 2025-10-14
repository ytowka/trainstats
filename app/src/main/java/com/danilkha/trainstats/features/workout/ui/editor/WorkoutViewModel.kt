package com.danilkha.trainstats.features.workout.ui.editor

import androidx.lifecycle.viewModelScope
import com.danilkha.trainstats.core.viewmodel.MviViewModel
import com.danilkha.trainstats.features.exercises.ui.ExerciseModel
import com.danilkha.trainstats.features.workout.domain.model.Kg
import com.danilkha.trainstats.features.workout.domain.usecase.ArchiveWorkoutUseCase
import com.danilkha.trainstats.features.workout.domain.usecase.CommitWorkoutSaveUseCase
import com.danilkha.trainstats.features.workout.domain.usecase.GetWorkoutByIdUseCase
import com.danilkha.trainstats.features.workout.domain.usecase.SaveWorkoutUseCase
import com.danilkha.trainstats.features.workout.domain.model.WorkoutParams
import com.danilkha.trainstats.features.workout.domain.usecase.GetExerciseHistoryUseCase
import com.danilkha.trainstats.features.workout.ui.ExerciseGroup
import com.danilkha.trainstats.features.workout.ui.ExerciseSetSlot
import com.danilkha.trainstats.features.workout.ui.RepetitionsModel
import com.danilkha.trainstats.features.workout.ui.SET_DELETE_DELAY
import com.danilkha.trainstats.features.workout.ui.Side
import com.danilkha.trainstats.features.workout.ui.WorkoutModel
import com.danilkha.trainstats.features.workout.ui.isNotEmpty
import com.danilkha.trainstats.features.workout.ui.toModel
import com.danilkha.uikit.components.move
import korlibs.time.DateTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class WorkoutViewModel @Inject constructor(
    private val workoutSaver: WorkoutSaver,
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    private val commitWorkoutSaveUseCase: CommitWorkoutSaveUseCase,
    private val getWorkoutByIdUseCase: GetWorkoutByIdUseCase,
    private val archiveWorkoutUseCase: ArchiveWorkoutUseCase,
    private val getExerciseHistoryUseCase: GetExerciseHistoryUseCase,
) : MviViewModel<WorkoutState, WorkoutEvent, WorkoutSideEffect>() {

    override val startState: WorkoutState = WorkoutState()

    private var tempIndexes: Long = 0
        get() {
            field++
            return field
        }

    private val pendingDeletingSetMap = mutableMapOf<Long, Job>()

    override fun reduce(
        state: WorkoutState,
        event: WorkoutEvent
    ): WorkoutState {
        return when (event) {
            is WorkoutEvent.InitState -> event.state
            is WorkoutEvent.AddExercise -> state.reduceAddExercise(event.exercise)
            is WorkoutEvent.ChangeDate -> state.copy(date = event.date)
            is WorkoutEvent.DeleteGroup -> state.copy(
                groups = state.groups.filterIndexed { index, exerciseGroup -> index != event.groupIndex }
            )
            is WorkoutEvent.DeleteSet -> {
                val group = state.groups[event.groupIndex]
                val id = group.sets[event.setIndex].tempId
                state.copy(pendingDelete = state.pendingDelete + id)
            }
            is WorkoutEvent.CommitDeleteSet -> {
                val groups = state.groups.map { group ->
                    group.copy(
                        sets = group.sets.filterNot { it.tempId == event.setId }
                    )
                }
                state.copy(groups = groups, pendingDelete = state.pendingDelete - event.setId)
            }
            is WorkoutEvent.EditReps -> state.reduceEditReps(
                groupIndex = event.groupIndex,
                setIndex = event.setIndex,
                side = event.side,
                reps = event.reps
            )
            is WorkoutEvent.EditWeight -> state.reduceEditWeight(
                groupIndex = event.groupIndex,
                setIndex = event.setIndex,
                kg = event.kg
            )
            is WorkoutEvent.OnGroupMove -> state.reduceGroupMove(event.from, event.to)
            is WorkoutEvent.OnSetMove -> state.reduceSetMove(
                groupIndex = event.groupIndex,
                from = event.from,
                to = event.to
            )
            is WorkoutEvent.ReturnDeletedSet -> {
                val setId = state.groups[event.groupIndex].sets[event.setIndex].tempId
                state.copy(pendingDelete = state.pendingDelete - setId)
            }
            is WorkoutEvent.ToggleGroup -> state.reduceToggleGroup(event.groupIndex)
            else -> state
        }
    }

    override suspend fun afterReduce(
        newState: WorkoutState,
        event: WorkoutEvent
    ) {
        when (event) {
            is WorkoutEvent.RequestInit -> init(event.editingId)
            is WorkoutEvent.DeleteSet -> {
                val group = newState.groups[event.groupIndex]
                val set = group.sets[event.setIndex]
                pendingDeletingSetMap[set.tempId] = viewModelScope.launch {
                    if(set.isNotEmpty) {
                        delay(SET_DELETE_DELAY)
                    }
                    processEvent(WorkoutEvent.CommitDeleteSet(set.tempId))
                }
            }
            is WorkoutEvent.CommitDeleteSet -> {
                pendingDeletingSetMap.remove(event.setId)
            }
            is WorkoutEvent.ReturnDeletedSet -> {
                val setId = newState.groups[event.groupIndex].sets[event.setIndex].tempId
                pendingDeletingSetMap[setId]?.cancel()
                pendingDeletingSetMap.remove(setId)
            }
            is WorkoutEvent.DeleteWorkout -> deleteWorkout()
            else -> Unit
        }
        when (event) {
            is WorkoutEvent.CommitDeleteSet,
            is WorkoutEvent.DeleteGroup,
            is WorkoutEvent.OnGroupMove,
            is WorkoutEvent.OnSetMove,
            is WorkoutEvent.EditReps,
            is WorkoutEvent.EditWeight,
            is WorkoutEvent.AddExercise,
            is WorkoutEvent.ChangeDate -> workoutSaver.update(newState.mapToParams())
            else -> Unit
        }
    }

    private fun init(editingId: Long?) {
        viewModelScope.launch {
            if (editingId != null) {
                getWorkoutByIdUseCase(editingId).onSuccess { workout ->
                    val workoutModel = workout.toModel { tempIndexes }
                    val state = startState.copy(
                        initialWorkout = workoutModel,
                        date = workoutModel.dateTime.date,
                        groups = workoutModel.groups,
                        initialized = true,
                        initialization = WorkoutEditorInitialization.EDIT
                    )
                    processEvent(WorkoutEvent.InitState(state))
                }
            } else {
                val id = saveWorkoutUseCase(
                    WorkoutParams(
                        id = null,
                        date = DateTime.now().date,
                        steps = emptyList()
                    )
                ).getOrThrow()
                val state = startState.copy(
                    initialWorkout = WorkoutModel(
                        id = id,
                        dateTime = DateTime.now(),
                        groups = emptyList(),
                        saved = false
                    ),
                    initialized = true,
                    initialization = WorkoutEditorInitialization.NEW
                )
                processEvent(WorkoutEvent.InitState(state))
            }
        }
    }

    private fun WorkoutState.reduceToggleGroup(groupIndex: Int): WorkoutState {
        val groupId = groups[groupIndex].groupTempId
        val isOpened = groupId in collapsedGroupIds
        return copy(
            collapsedGroupIds = if (isOpened) {
                collapsedGroupIds - groupId
            } else {
                collapsedGroupIds + groupId
            }
        )
    }

    private fun WorkoutState.reduceAddExercise(exercise: ExerciseModel): WorkoutState {
        val group = ExerciseGroup(
            groupTempId = tempIndexes,
            exerciseId = exercise.id,
            name = exercise.name,
            imageUrl = exercise.imageUrl,
            hasWeight = exercise.hasWeight,
            separated = exercise.separated,
            sets = listOf(ExerciseSetSlot.Stub(tempIndexes))
        )
        return copy(
            groups = groups + group
        )
    }

    private fun WorkoutState.reduceEditWeight(groupIndex: Int, setIndex: Int, kg: Float): WorkoutState {
        val group = groups[groupIndex]
        val set = group.sets[setIndex]

        return when (set) {
            is ExerciseSetSlot.ExerciseSetModel -> copy(
                groups = updateGroupsWithSets(
                    groupIndex,
                    group.sets.replace(
                        setIndex, set.copy(
                            weight = Kg(kg)
                        )
                    ),
                )
            )
            is ExerciseSetSlot.Stub -> {
                val newSet = ExerciseSetSlot.ExerciseSetModel(
                    tempId = set.tempId,
                    reps = when (group.separated) {
                        true -> RepetitionsModel.Double(null, null)
                        false -> RepetitionsModel.Single(null)
                    },
                    weight = Kg(kg)
                )
                val newSets = group.sets.toMutableList()
                    .apply {
                        removeAt(lastIndex)
                        add(newSet)
                        add(ExerciseSetSlot.Stub(tempIndexes))
                    }
                copy(
                    groups = updateGroupsWithSets(groupIndex, newSets)
                )
            }
        }
    }

    private fun WorkoutState.reduceEditReps(groupIndex: Int, setIndex: Int, side: Side?, reps: Float): WorkoutState {
        val group = groups[groupIndex]
        val set = group.sets[setIndex]

        return when (set) {
            is ExerciseSetSlot.ExerciseSetModel -> {
                val newReps = when (set.reps) {
                    is RepetitionsModel.Double -> when (side) {
                        Side.Left -> RepetitionsModel.Double(reps, set.reps.right)
                        Side.Right -> RepetitionsModel.Double(set.reps.left, reps)
                        null -> RepetitionsModel.Double(reps, set.reps.right)
                    }
                    is RepetitionsModel.Single -> RepetitionsModel.Single(reps)
                }
                copy(
                    groups = groups.replace(
                        groupIndex, group.copy(
                            sets = group.sets.replace(
                                setIndex, set.copy(
                                    reps = newReps
                                )
                            )
                        )
                    )
                )
            }
            is ExerciseSetSlot.Stub -> {
                val newReps = when (group.separated) {
                    true -> when (side) {
                        Side.Left -> RepetitionsModel.Double(reps, null)
                        Side.Right -> RepetitionsModel.Double(null, reps)
                        null -> RepetitionsModel.Double(reps, null)
                    }
                    false -> RepetitionsModel.Single(reps)
                }
                val newSet = ExerciseSetSlot.ExerciseSetModel(
                    tempId = set.tempId,
                    reps = newReps,
                    weight = null
                )
                val newSets = group.sets.toMutableList()
                    .apply {
                        removeAt(lastIndex)
                        add(newSet)
                        add(ExerciseSetSlot.Stub(tempIndexes))
                    }
                copy(
                    groups = updateGroupsWithSets(groupIndex, newSets)
                )
            }
        }
    }

    private fun WorkoutState.updateGroupsWithSets(groupIndex: Int, sets: List<ExerciseSetSlot>): List<ExerciseGroup> {
        val group = groups[groupIndex]
        return groups.replace(groupIndex, group.copy(sets = sets))
    }

    private fun WorkoutState.reduceSetMove(groupIndex: Int, from: Int, to: Int): WorkoutState {
        val group = groups[groupIndex]
        return if (to < group.sets.size - 1) {
            copy(
                groups = groups.replace(
                    groupIndex, group.copy(
                        sets = group.sets.toMutableList().apply { move(from, to) }.toList()
                    )
                )
            )
        } else this
    }

    private fun WorkoutState.reduceGroupMove(from: Int, to: Int): WorkoutState {
        return copy(
            groups = groups.toMutableList().apply { move(from, to) }.toList()
        )
    }

    private fun deleteWorkout() {
        viewModelScope.launch {
            _state.value.initialWorkout?.id?.let { id ->
                archiveWorkoutUseCase(id).onSuccess {
                    showSideEffect(WorkoutSideEffect.Deleted)
                }
            }
        }
    }

    override fun onCleared() {
        workoutSaver.commit(_state.value.mapToParams())
    }
}

private fun <T> List<T>.replace(index: Int, item: T) = toMutableList().apply {
    set(index, item)
}.toList()



