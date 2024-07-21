package com.danilkha.trainstats.features.workout.ui

import com.danilkha.trainstats.core.viewmodel.BaseViewModel
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import kotlin.properties.Delegates.notNull

class WorkoutViewModel : BaseViewModel<WorkoutState, WorkoutSideEffect>(){

    override val startState: WorkoutState = WorkoutState()


    fun init(editingId: Long?){

    }

    fun toggleGroup(){

    }

    fun addSet(groupIndex: Int){

    }

    fun addExercise(exercise: ExerciseData){

    }

    fun editWeight(kg: Float, groupIndex: Int, setIndex: Int){

    }

    fun editReps(reps: Float, groupIndex: Int, setIndex: Int, side: Side?){

    }

    fun onSetMove(groupIndex: Int, from: Int, to: Int){

    }

    fun onGroupMove(from: Int, to: Int){

    }

    fun deleteSet(groupIndex: Int, setIndex: Int){

    }

    fun returnDeletedxSet(groupIndex: Int, setIndex: Int){

    }

    fun deleteWorkout(){

    }

    fun saveWorkout(){

    }
}


