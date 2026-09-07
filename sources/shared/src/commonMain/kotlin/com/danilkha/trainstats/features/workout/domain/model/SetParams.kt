package com.danilkha.trainstats.features.workout.domain.model

data class SetParams(
    val exerciseId: String,
    val exerciseName: String,
    val reps: Repetitions,
    val weight: Kg?,
)