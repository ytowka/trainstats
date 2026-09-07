package com.danilkha.trainstats.features.workout.ui.history

import com.danilkha.commoncore.viewmodel.MviViewModel
import com.danilkha.trainstats.features.workout.domain.usecase.GetWorkoutHistoryUseCase

class HistoryViewModel(
    private val getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase,
) : MviViewModel<HistoryState, HistoryEvent, HistorySideEffect>() {

    override suspend fun loadData() {
        getWorkoutHistoryUseCase().collectResult { workouts ->
            processEvent(HistoryEvent.DataLoaded(workouts))
        }
    }

    override fun reduce(state: HistoryState, event: HistoryEvent): HistoryState {
        return when(event) {
            is HistoryEvent.DataLoaded -> state.copy(
                allWorkouts = event.workouts.map { workout ->
                    WorkoutHistoryModel(
                        id = workout.id,
                        date = workout.dateTime,
                        exercises = workout.exercises
                    )
                }
            )
            is HistoryEvent.ChangeSearchQuery -> state.copy(searchQuery = event.query)
        }
    }

    override val startState: HistoryState = HistoryState()
}