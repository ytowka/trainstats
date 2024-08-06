package com.danilkha.trainstats.features.exercises.ui.history

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.workout.domain.model.Kg
import com.danilkha.trainstats.features.workout.domain.model.Repetitions
import com.danilkha.trainstats.features.workout.ui.RepetitionsModel
import korlibs.time.Date

data class ExerciseSetHistoryModel(
    val id: Long = 0,
    val workoutId: Long,
    val reps: RepetitionsModel,
    val weight: Kg?,
)

data class ExerciseHistoryModel(
    val date: Date,
    val sets: List<ExerciseSetHistoryModel>
)