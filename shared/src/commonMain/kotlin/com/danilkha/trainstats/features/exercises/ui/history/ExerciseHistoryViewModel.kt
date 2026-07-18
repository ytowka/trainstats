package com.danilkha.trainstats.features.exercises.ui.history

import com.danilkha.commoncore.utils.toLocal
import com.danilkha.commoncore.viewmodel.MviViewModel
import com.danilkha.trainstats.features.workout.domain.usecase.GetExerciseHistoryUseCase
import com.danilkha.trainstats.features.workout.ui.toModel

class ExerciseHistoryViewModel(
    private val getExerciseHistoryUseCase: GetExerciseHistoryUseCase,
): MviViewModel<ExerciseHistoryState, ExerciseHistoryEvent, ExerciseHistorySingleEvent>(){
    override val startState: ExerciseHistoryState = ExerciseHistoryState()

    override fun reduce(
        state: ExerciseHistoryState,
        event: ExerciseHistoryEvent
    ): ExerciseHistoryState {
        return when (event) {
            is ExerciseHistoryEvent.HistoryLoaded -> state.copy(
                exerciseName = event.exerciseName,
                list = event.list
            )
            else -> state
        }
    }

    override suspend fun afterReduce(newState: ExerciseHistoryState, event: ExerciseHistoryEvent) {
        when (event) {
            is ExerciseHistoryEvent.Init -> {
                getExerciseHistoryUseCase(event.exerciseId).onSuccess { workouts ->
                    val list = workouts.exercises.map { set ->
                        ExerciseHistoryModel(
                            date = set.date.toLocal().date,
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
                    processEvent(ExerciseHistoryEvent.HistoryLoaded(
                        exerciseName = workouts.exerciseName,
                        list = list
                    ))
                }
            }
            else -> {}
        }
    }
}