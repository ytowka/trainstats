package com.danilkha.trainstats.features.workout.domain.model

data class SetParams(
    val exerciseId: Long,
    val exerciseName: String,
    val reps: Repetitions,
    val weight: Kg?,
)