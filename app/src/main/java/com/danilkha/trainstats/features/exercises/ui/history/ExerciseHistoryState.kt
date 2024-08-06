package com.danilkha.trainstats.features.exercises.ui.history

import com.danilkha.trainstats.features.workout.domain.model.ExerciseSet
import com.danilkha.trainstats.features.workout.ui.ExerciseSetSlot
import korlibs.time.Date

data class ExerciseHistoryState(
    val exerciseName: String = "",
    val list: List<ExerciseHistoryModel> = emptyList()
) {
}

sealed interface ExerciseHistorySingleEvent{

}