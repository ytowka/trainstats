package com.danilkha.trainstats.features.exercises.ui

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import kotlinx.datetime.Instant

data class ExerciseModel(
    val id: String,
    val name: String,
    val separated: Boolean,
    val imageUrl: String?,
    val hasWeight: Boolean,
    val lastUsedDate: Instant? = null,
)

fun ExerciseData.toModel() = ExerciseModel(
    id, name, separated, imageUrl, hasWeight, lastUsedDate,
)