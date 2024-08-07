package com.danilkha.trainstats.features.workout.domain.model

import korlibs.time.Date

class ExerciseWorkout(
    val date: Date,
    val sets: List<ExerciseSet>
)