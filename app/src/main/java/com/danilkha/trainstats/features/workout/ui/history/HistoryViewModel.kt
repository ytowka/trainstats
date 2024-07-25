package com.danilkha.trainstats.features.workout.ui.history

import androidx.lifecycle.viewModelScope
import com.danilkha.trainstats.core.viewmodel.BaseViewModel
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.workout.domain.usecase.GetWorkoutHistoryUseCase
import com.danilkha.trainstats.features.workout.ui.toModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class HistoryViewModel @Inject constructor(
    private val getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase,
): BaseViewModel<HistoryState, HistoryEvent>(){

    override val startState: HistoryState = HistoryState()

    init {
        viewModelScope.launch {
            getWorkoutHistoryUseCase().collectResult { workouts ->
                update { state ->
                    state.copy(workouts = workouts.map { workout ->

                        WorkoutHistoryModel(
                            id = workout.id,
                            date = workout.dateTime,
                            exercises = workout.exercises
                        )
                    })
                }
            }
        }
    }
}