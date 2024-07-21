package com.danilkha.trainstats.features.workout.domain.model

import korlibs.time.DateTime

data class Workout(
    val id: Long = 0,
    val dateTime: DateTime,
    val steps: List<ExerciseSet>
)
