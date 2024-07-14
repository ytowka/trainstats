package com.danilkha.trainstats.features.workout.domain.model

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseSet
import korlibs.time.DateTime

data class Workout(
    val id: Long = 0,
    val dateTime: DateTime,
    val steps: List<ExerciseSet>
)
