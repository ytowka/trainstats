package com.danilkha.trainstats.features.exercises.ui

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import korlibs.time.DateTime

data class ExerciseModel(
    val id: Long,
    val name: String,
    val separated: Boolean,
    val imageUrl: String?,
    val hasWeight: Boolean,
    val lastUsedDate: DateTime? = null,
)

fun ExerciseData.toModel() = ExerciseModel(
    id, name, separated, imageUrl, hasWeight, lastUsedDate,
)