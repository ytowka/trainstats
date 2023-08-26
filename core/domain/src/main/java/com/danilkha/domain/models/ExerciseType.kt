package com.danilkha.domain.models

sealed interface ExerciseType{
    class Weight(
        val weight: Kg
    ) : ExerciseType

    object BodyWeight : ExerciseType
}
