package com.danilkha.domain.models

data class ExerciseData(
    val id: Long = 0,
    val name: String,
    val imageUrl: String,
    val type: ExerciseType,
)
