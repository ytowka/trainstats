package com.danilkha.trainstats.features.exercises.ui


data class ExerciseListState(
    val searchQuery: String = "",
    val exerciseList: List<ExerciseModel> = emptyList()
)

sealed interface ExerciseListSideEffect{
    class ExerciseClicked(val exerciseModel: ExerciseModel) : ExerciseListSideEffect
}
