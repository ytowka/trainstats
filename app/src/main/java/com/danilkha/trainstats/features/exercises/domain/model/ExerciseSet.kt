package com.danilkha.trainstats.features.exercises.domain.model

import korlibs.time.DateTime
import korlibs.time.TimeSpan


data class ExerciseSet(
    val id: Long = 0,
    val workoutId: Long,
    val dateTime: DateTime,
    val duration: TimeSpan,
    val exerciseData: ExerciseData,
    val reps: Int,
)

