package com.danilkha.trainstats.features.workout.domain.model

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData


data class ExerciseSet(
    val id: String = "",
    val workoutId: String,
    val exerciseData: ExerciseData,
    val reps: Repetitions,
    val weight: Kg?,
    val orderPosition: Int,
    val variationId: String? = null,
    val variationName: String? = null,
)

sealed interface Repetitions{
    data class Single(val reps: Float) : Repetitions
    data class Double(val left: Float, val right: Float) : Repetitions
}

