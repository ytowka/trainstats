package com.danilkha.trainstats.features.exercises.ui.history

data class ExerciseHistoryState(
    val exerciseName: String = "",
    val list: List<ExerciseHistoryModel> = emptyList()
)

sealed interface ExerciseHistorySingleEvent{

}