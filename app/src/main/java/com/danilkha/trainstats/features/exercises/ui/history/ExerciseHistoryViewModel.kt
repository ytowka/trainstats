package com.danilkha.trainstats.features.exercises.ui.history

import androidx.lifecycle.viewModelScope
import com.danilkha.trainstats.core.viewmodel.BaseViewModel
import com.danilkha.trainstats.features.workout.domain.usecase.GetExerciseHistoryUseCase
import com.danilkha.trainstats.features.workout.ui.toModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExerciseHistoryViewModel @Inject constructor(
    private val getExerciseHistoryUseCase: GetExerciseHistoryUseCase,
): BaseViewModel<ExerciseHistoryState, ExerciseHistorySingleEvent>(){
    override val startState: ExerciseHistoryState = ExerciseHistoryState()


    fun init(exerciseId: Long){
        viewModelScope.launch {
            getExerciseHistoryUseCase(exerciseId).onSuccess { workouts ->
                update { state ->
                    state.copy(
                        exerciseName = workouts.exerciseName,
                        list = workouts.exercises.map { set ->
                            ExerciseHistoryModel(
                                date = set.date,
                                sets = set.sets.map {
                                    ExerciseSetHistoryModel(
                                        id = it.id,
                                        workoutId = it.workoutId,
                                        reps = it.reps.toModel(),
                                        weight = it.weight
                                    )
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}