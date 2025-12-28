package com.danilkha.trainstats.features.exercises.ui

sealed interface ExerciseListEvent {

    data class UpdateExerciseList(val exercises: List<ExerciseModel>) : ExerciseListEvent
    data class ChangeSearchQuery(val text: String) : ExerciseListEvent
    data object UpdateList : ExerciseListEvent
    data class OnExerciseClicked(val exerciseModel: ExerciseModel): ExerciseListEvent
}