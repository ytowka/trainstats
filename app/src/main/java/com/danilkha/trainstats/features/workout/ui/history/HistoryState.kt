package com.danilkha.trainstats.features.workout.ui.history

import com.danilkha.trainstats.features.workout.ui.WorkoutModel

data class HistoryState(
    val workouts: List<WorkoutHistoryModel> = emptyList()
) {
}

sealed interface HistoryEvent{

}