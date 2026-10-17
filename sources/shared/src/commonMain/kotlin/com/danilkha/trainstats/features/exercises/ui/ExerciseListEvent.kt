package com.danilkha.trainstats.features.exercises.ui

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseVariation

sealed interface ExerciseListEvent {

    data class UpdateExerciseList(val exercises: List<ExerciseModel>) : ExerciseListEvent
    data class ChangeSearchQuery(val text: String) : ExerciseListEvent
    data object UpdateList : ExerciseListEvent
    data class OnExerciseClicked(
        val exerciseModel: ExerciseModel,
        val variation: ExerciseVariation? = exerciseModel.variations.firstOrNull { it.isDefault },
    ) : ExerciseListEvent
    data object OnSelectorClosed : ExerciseListEvent
}