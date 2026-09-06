package com.danilkha.trainstats.features.workout.domain.model

import kotlinx.datetime.Instant

data class Workout(
    val id: String = "",
    val dateTime: Instant,
    val steps: List<ExerciseSet>,
    val saved: Boolean,
    val archived: Boolean,
    val lastEdited: Instant? = null,
)
