package com.danilkha.trainstats.features.workout.ui.history

import com.danilkha.trainstats.features.workout.domain.model.WorkoutPreview

data class HistoryState(
    val workouts: List<WorkoutHistoryModel> = emptyList(),
    val calendarOpened: Boolean = false,
    val searchQuery: String = "",
) {
}

sealed interface HistorySideEffect {

}

sealed interface HistoryEvent {

    data class DataLoaded(
        val workouts: List<WorkoutPreview>
    ) : HistoryEvent

    data class ChangeSearchQuery(
        val query: String
    ) : HistoryEvent
}