package com.danilkha.trainstats.features.workout.ui

import androidx.compose.ui.util.trace
import androidx.lifecycle.viewModelScope
import com.danilkha.trainstats.core.utils.format2
import com.danilkha.trainstats.core.viewmodel.BaseViewModel
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.workout.domain.model.Kg
import com.danilkha.trainstats.features.workout.ui.components.ExerciseSet
import com.danilkha.uikit.components.move
import korlibs.time.Date
import korlibs.time.DateTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class WorkoutViewModel : BaseViewModel<WorkoutState, WorkoutSideEffect>(){

    override val startState: WorkoutState = WorkoutState()

    private var tempIndexes: Long = 0
        get() {
            field++
            return field
        }


    fun init(editingId: Long?){

    }

    fun changeDate(date: Date){
        _state.update {
            it.copy(date = date)
        }
    }

    fun toggleGroup(groupIndex: Int){
        update{
            val groupId = it.groups[groupIndex].groupTempId
            val isOpened = groupId in it.collapsedGroupIds
            it.copy(
                collapsedGroupIds = if (isOpened) {
                    it.collapsedGroupIds - groupId
                }else {
                    it.collapsedGroupIds + groupId
                }
            )
        }
    }

    fun addExercise(exercise: ExerciseData?){
        val group = ExerciseGroup(
            groupTempId = tempIndexes,
            exerciseId = 0,
            name = "Bench press $tempIndexes",
            imageUrl = null,
            hasWeight = true,
            separated = false,
            sets = listOf(ExerciseSetSlot.Stub(tempIndexes))
        )
        update {
            it.copy(
                groups = it.groups + group
            )
        }
    }

    fun editWeight(groupIndex: Int, setIndex: Int, kg: Float){
        update {
            val group = it.groups[groupIndex]
            val set = group.sets[setIndex]

            when (set){
                is ExerciseSetSlot.ExerciseSetModel -> it.copy(
                    groups = it.updateGroupsWithSets(
                        groupIndex, group.sets.replace(setIndex, set.copy(
                            weight = Kg(kg)
                        )
                    ),)
                )
                is ExerciseSetSlot.Stub -> {
                    val newSet = ExerciseSetSlot.ExerciseSetModel(
                        tempId = set.tempId,
                        dateTime = DateTime.now(),
                        reps = when (group.separated) {
                            true -> RepetitionsModel.Double(null, null)
                            false -> RepetitionsModel.Single(null)
                        },
                        weight = Kg(kg)
                    )
                    val newSets = group.sets.toMutableList()
                        .apply {
                            removeLast()
                            add(newSet)
                            add(ExerciseSetSlot.Stub(tempIndexes))
                        }
                    it.copy(
                        groups = it.updateGroupsWithSets(groupIndex, newSets)
                    )
                }
            }

        }
    }

    fun editReps(groupIndex: Int, setIndex: Int, side: Side?, reps: Float){
        update {
            val group = it.groups[groupIndex]
            val set = group.sets[setIndex]

            when (set){
                is ExerciseSetSlot.ExerciseSetModel -> {
                    val newReps = when(set.reps){
                        is RepetitionsModel.Double -> when(side){
                            Side.Left -> RepetitionsModel.Double(reps, set.reps.right)
                            Side.Right ->  RepetitionsModel.Double( set.reps.left, reps)
                            null ->  RepetitionsModel.Double(reps, set.reps.right)
                        }
                        is RepetitionsModel.Single -> RepetitionsModel.Single(reps)
                    }
                    it.copy(
                        groups = it.groups.replace(groupIndex, group.copy(
                            sets = group.sets.replace(setIndex, set.copy(
                                reps = newReps
                            ))
                        ))
                    )
                }
                is ExerciseSetSlot.Stub -> {
                    val newReps = when(group.separated){
                        true -> when(side){
                            Side.Left -> RepetitionsModel.Double(reps, null)
                            Side.Right ->  RepetitionsModel.Double( null, reps)
                            null ->  RepetitionsModel.Double(reps, null)
                        }
                        false -> RepetitionsModel.Single(reps)
                    }
                    val newSet = ExerciseSetSlot.ExerciseSetModel(
                        tempId = set.tempId,
                        dateTime = DateTime.now(),
                        reps = newReps,
                        weight = null
                    )
                    val newSets = group.sets.toMutableList()
                        .apply {
                            removeLast()
                            add(newSet)
                            add(ExerciseSetSlot.Stub(tempIndexes))
                        }
                    it.copy(
                        groups = it.updateGroupsWithSets(groupIndex, newSets)
                    )
                }
            }
        }
    }

    private fun WorkoutState.updateGroupsWithSets(groupIndex: Int, sets: List<ExerciseSetSlot>): List<ExerciseGroup>{
        val group = groups[groupIndex]
        return groups.replace(groupIndex, group.copy(sets = sets))
    }

    fun onSetMove(groupIndex: Int, from: Int, to: Int){
        update {
            val group = it.groups[groupIndex]
            if(to < group.sets.size-1){
                it.copy(
                    groups = it.groups.replace(groupIndex, group.copy(
                        sets = group.sets.toMutableList().apply { move(from, to) }.toList()
                    ))
                )
            }else it
        }
    }

    fun onGroupMove(from: Int, to: Int) = update {
        it.copy(
            groups = it.groups.toMutableList().apply { move(from, to) }.toList()
        )
    }

    private val pendingDeletingSetMap = mutableMapOf<Long, Job>()

    fun deleteSet(groupIndex: Int, setIndex: Int){
        update {
            val group = it.groups[groupIndex]
            val id = group.sets[setIndex].tempId
            pendingDeletingSetMap[id] = viewModelScope.launch {
                delay(SET_DELETE_DELAY)
                commitSetDelete(id)
            }
            it.copy(pendingDelete = it.pendingDelete + id)
        }
    }

    fun deleteGroup(groupIndex: Int,){
        update {
            it.copy(
                groups = it.groups.filterIndexed { index, exerciseGroup -> index != groupIndex }
            )
        }
    }

    private fun commitSetDelete(setId: Long){
        pendingDeletingSetMap.remove(setId)
        update {
            val groups = it.groups.map { group ->
                group.copy(
                    sets = group.sets.filterNot { it.tempId == setId }
                )
            }
            it.copy(groups = groups, pendingDelete = it.pendingDelete - setId)
        }
    }

    fun returnDeletedSet(groupIndex: Int, setIndex: Int){
        update {
            val setId = it.groups[groupIndex].sets[setIndex].tempId
            pendingDeletingSetMap[setId]?.cancel()
            pendingDeletingSetMap.remove(setId)
            it.copy(pendingDelete = it.pendingDelete - setId)
        }
    }

    fun deleteWorkout(){

    }

    fun saveWorkout(){

    }
}

private fun <T> List<T>.replace(index: Int, item: T) = toMutableList().apply {
    set(index, item)
}.toList()



