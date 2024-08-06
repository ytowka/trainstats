package com.danilkha.trainstats.features.exercises.ui.history

import com.danilkha.trainstats.core.viewmodel.BaseViewModel
import javax.inject.Inject

class ExerciseHistoryViewModel @Inject constructor(

): BaseViewModel<ExerciseHistoryState, ExerciseHistorySingleEvent>(){
    override val startState: ExerciseHistoryState = ExerciseHistoryState()


    fun init(exerciseId: Long){

    }
}