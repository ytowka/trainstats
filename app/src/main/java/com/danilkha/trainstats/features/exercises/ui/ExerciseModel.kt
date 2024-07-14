package com.danilkha.trainstats.features.exercises.ui

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData

data class ExerciseModel(
    val id: Long,
    val name: String,
    val separated: Boolean,
    val hasWeight: Boolean,
)

fun ExerciseData.toModel() = ExerciseModel(
    id, name, separated, hasWeight
)