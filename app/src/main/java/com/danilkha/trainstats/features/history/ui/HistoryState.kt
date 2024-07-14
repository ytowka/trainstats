package com.danilkha.trainstats.features.history.ui

data class HistoryState(
    val workouts: List<String> = emptyList()
) {
}

sealed interface HistoryEvent{

}