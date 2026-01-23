package com.danilkha.trainstats.features.workout.domain.model

import korlibs.time.Date
import korlibs.time.DateTime

data class WorkoutParams(
    val id: Long?,
    val date: Date,
    val steps: List<SetParams>,
    val lastEdited: DateTime? = null,
)