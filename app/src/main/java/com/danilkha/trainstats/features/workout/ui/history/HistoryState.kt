package com.danilkha.trainstats.features.workout.ui.history

import com.danilkha.trainstats.features.workout.domain.model.WorkoutPreview

data class HistoryState(
    val allWorkouts: List<WorkoutHistoryModel> = emptyList(),
    val calendarOpened: Boolean = false,
    val searchQuery: String = "",
) {
    private val filteredWorkouts by lazy {
        allWorkouts.filter {
            it.exercises.any {
                it.contains(searchQuery)
            }
        }
    }

    val workouts: List<WorkoutHistoryModel> = if(searchQuery.isEmpty()) {
        allWorkouts
    } else {
        filteredWorkouts
    }
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