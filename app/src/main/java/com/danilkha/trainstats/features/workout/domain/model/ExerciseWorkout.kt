package com.danilkha.trainstats.features.workout.domain.model

import kotlinx.datetime.Instant

class ExerciseWorkout(
    val date: Instant,
    val sets: List<ExerciseSet>
)