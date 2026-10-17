package com.danilkha.trainstats.features.exercises.domain.model

data class ExerciseVariation(
    val id: String,
    val exerciseId: String,
    val name: String,
    val isDefault: Boolean = false,
)

fun normalizedVariationName(name: String): String = name.trim().lowercase()

fun validVariationNames(variations: List<ExerciseVariation>): Boolean =
    variations.all { it.name.isNotBlank() } &&
        variations.map { normalizedVariationName(it.name) }.distinct().size == variations.size
