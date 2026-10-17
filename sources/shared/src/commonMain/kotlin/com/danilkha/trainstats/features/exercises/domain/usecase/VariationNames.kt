package com.danilkha.trainstats.features.exercises.domain.usecase

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.exercises.domain.model.validVariationNames

internal fun normalizeVariationName(name: String): String =
    name.trim().lowercase().replaceFirstChar { it.titlecase() }

internal fun ExerciseData.withNormalizedVariations(): ExerciseData {
    require(validVariationNames(variations)) { "Variation names must be nonblank and unique" }
    return copy(variations = variations.map { it.copy(name = normalizeVariationName(it.name)) })
}
