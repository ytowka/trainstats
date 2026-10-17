package com.danilkha.trainstats.features.exercises.ui

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseVariation

data class ExerciseListState(
    val searchQuery: String = "",
    val exerciseList: List<ExerciseModel> = emptyList()
)

sealed interface ExerciseListSideEffect{
    class ExerciseClicked(
        val exerciseModel: ExerciseModel,
        val variation: ExerciseVariation?,
    ) : ExerciseListSideEffect
}
