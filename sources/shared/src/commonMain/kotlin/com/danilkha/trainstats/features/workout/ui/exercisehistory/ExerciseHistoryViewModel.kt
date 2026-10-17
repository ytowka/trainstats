package com.danilkha.trainstats.features.workout.ui.exercisehistory

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
            is ExerciseHistoryEvent.Init -> startState.copy(currentVariationId = event.variationId)
            is ExerciseHistoryEvent.OnlyCurrentVariation -> state.copy(onlyCurrentVariation = event.checked)
            is ExerciseHistoryEvent.HistoryLoaded -> state.copy(
                exerciseName = event.exerciseName,
                list = event.list
            )
        }
    }

    override suspend fun afterReduce(newState: ExerciseHistoryState, event: ExerciseHistoryEvent) {
        when (event) {
            is ExerciseHistoryEvent.Init -> {
                getExerciseHistoryUseCase(event.exerciseId).onSuccess { workouts ->
                    val list = workouts.exercises.map { set ->
                        ExerciseHistoryModel(
                            date = set.date.toLocal().date,
                            sets = set.sets.mapIndexed { index, item ->
                                val previous = set.sets.getOrNull(index - 1)
                                ExerciseSetHistoryModel(
                                    id = item.id,
                                    workoutId = item.workoutId,
                                    reps = item.reps.toModel(),
                                    weight = item.weight,
                                    variationId = item.variationId,
                                    variationName = item.variationName,
                                    startsGroup = previous == null || previous.variationId != item.variationId ||
                                        previous.orderPosition + 1 != item.orderPosition
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