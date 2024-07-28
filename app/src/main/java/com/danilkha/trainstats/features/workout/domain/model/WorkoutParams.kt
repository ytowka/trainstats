package com.danilkha.trainstats.features.workout.domain.model

import korlibs.time.Date

data class WorkoutParams(
    val id: Long?,
    val date: Date,
    val steps: List<SetParams>,
)