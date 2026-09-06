package com.danilkha.trainstats.features.exercises.ui.history

data class ExerciseHistoryState(
    val exerciseName: String = "",
    val list: List<ExerciseHistoryModel> = emptyList()
)

sealed interface ExerciseHistorySingleEvent{

}

sealed interface ExerciseHistoryEvent {

    data class Init(val exerciseId: String) : ExerciseHistoryEvent
    data class HistoryLoaded(val exerciseName: String, val list: List<ExerciseHistoryModel>) : ExerciseHistoryEvent
}