package com.danilkha.trainstats.features.exercises.ui.selector

import com.danilkha.commoncore.viewmodel.MviViewModel
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.usecase.CreateVariationUseCase

class VariationSelectorViewModel(
    private val repository: ExerciseRepository,
    private val createVariation: CreateVariationUseCase,
) : MviViewModel<VariationSelectorState, VariationSelectorEvent, VariationSelected>() {
    override val startState = VariationSelectorState()

    override fun reduce(state: VariationSelectorState, event: VariationSelectorEvent) = when (event) {
        is VariationSelectorEvent.Init -> startState.copy(exerciseId = event.exerciseId, selectedVariationId = event.selectedVariationId)
        is VariationSelectorEvent.Loaded -> if (event.exerciseId == state.exerciseId) state.copy(variations = event.variations, loading = false) else state
        is VariationSelectorEvent.EditName -> state.copy(name = event.name, failed = false)
        VariationSelectorEvent.Add -> state.copy(name = "")
        VariationSelectorEvent.Create -> state.copy(saving = state.canCreate)
        VariationSelectorEvent.Failed -> state.copy(saving = false, loading = false, failed = true)
        is VariationSelectorEvent.Select -> state.copy(selectedVariationId = event.variation?.id)
    }

    override suspend fun afterReduce(newState: VariationSelectorState, event: VariationSelectorEvent) {
        when (event) {
            is VariationSelectorEvent.Init -> runCatching { repository.getExercise(event.exerciseId) }
                .onSuccess { processEvent(VariationSelectorEvent.Loaded(event.exerciseId, it.variations)) }
                .onFailure { processEvent(VariationSelectorEvent.Failed) }
            VariationSelectorEvent.Create -> if (newState.saving) {
                createVariation(CreateVariationUseCase.Params(newState.exerciseId, newState.name.orEmpty()))
                    .onSuccess { showSideEffect(VariationSelected(it)) }
                    .onFailure { processEvent(VariationSelectorEvent.Failed) }
            }
            is VariationSelectorEvent.Select -> showSideEffect(VariationSelected(event.variation))
            else -> Unit
        }
    }
}
