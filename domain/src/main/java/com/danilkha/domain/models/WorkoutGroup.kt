package com.danilkha.domain.models

data class WorkoutGroup(
    val id: Long = 0,
    val name: String,
    val exercises: List<ExerciseData>,
)
