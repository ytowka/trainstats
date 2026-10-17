package com.danilkha.trainstats.features.exercises.ui.selector

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseVariation
import com.danilkha.trainstats.features.exercises.domain.model.normalizedVariationName

// The draft lives only until the next opening; creation persists only on Create.
data class VariationSelectorState(
    val exerciseId: String = "",
    val selectedVariationId: String? = null,
    val variations: List<ExerciseVariation> = emptyList(),
    val name: String? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val failed: Boolean = false,
) {
    val duplicate get() = name != null && variations.any { normalizedVariationName(it.name) == normalizedVariationName(name) }
    val canCreate get() = !name.isNullOrBlank() && !duplicate && !loading && !saving
}

sealed interface VariationSelectorEvent {
    data class Init(val exerciseId: String, val selectedVariationId: String? = null) : VariationSelectorEvent
    data class Loaded(val exerciseId: String, val variations: List<ExerciseVariation>) : VariationSelectorEvent
    data class EditName(val name: String) : VariationSelectorEvent
    data object Add : VariationSelectorEvent
    data object Create : VariationSelectorEvent
    data object Failed : VariationSelectorEvent
    data class Select(val variation: ExerciseVariation?) : VariationSelectorEvent
}

data class VariationSelected(val variation: ExerciseVariation?)

