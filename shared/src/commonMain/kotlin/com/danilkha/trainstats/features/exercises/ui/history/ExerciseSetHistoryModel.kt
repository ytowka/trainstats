package com.danilkha.trainstats.features.exercises.ui.history

import com.danilkha.trainstats.features.workout.domain.model.Kg
import com.danilkha.trainstats.features.workout.ui.RepetitionsModel
import kotlinx.datetime.LocalDate

data class ExerciseSetHistoryModel(
    val id: String = "",
    val workoutId: String,
    val reps: RepetitionsModel,
    val weight: Kg?,
)

data class ExerciseHistoryModel(
    val date: LocalDate,
    val sets: List<ExerciseSetHistoryModel>
)