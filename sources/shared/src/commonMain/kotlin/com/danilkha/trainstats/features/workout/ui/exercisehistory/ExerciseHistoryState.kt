package com.danilkha.trainstats.features.workout.ui.exercisehistory

data class ExerciseHistoryState(
    val exerciseName: String = "",
    val list: List<ExerciseHistoryModel> = emptyList(),
    val currentVariationId: String? = null,
    val onlyCurrentVariation: Boolean = false,
) {
    val visibleList get() = if (!onlyCurrentVariation) list else list.mapNotNull { entry ->
        entry.copy(sets = entry.sets.filter { it.variationId == currentVariationId }).takeIf { it.sets.isNotEmpty() }
    }
}

sealed interface ExerciseHistorySingleEvent{

}

sealed interface ExerciseHistoryEvent {

    data class Init(val exerciseId: String, val variationId: String? = null) : ExerciseHistoryEvent
    data class OnlyCurrentVariation(val checked: Boolean) : ExerciseHistoryEvent
    data class HistoryLoaded(val exerciseName: String, val list: List<ExerciseHistoryModel>) : ExerciseHistoryEvent
}