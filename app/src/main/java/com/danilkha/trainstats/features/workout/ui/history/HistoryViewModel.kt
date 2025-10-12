package com.danilkha.trainstats.features.workout.ui.history

import com.danilkha.trainstats.core.viewmodel.MviViewModel
import com.danilkha.trainstats.features.workout.domain.usecase.GetWorkoutHistoryUseCase
import javax.inject.Inject

class HistoryViewModel @Inject constructor(
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
                workouts = event.workouts.map { workout ->
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