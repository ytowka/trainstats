package com.danilkha.trainstats.features.workout.domain.model

import korlibs.time.DateTime

data class WorkoutPreview(
    val id: Long = 0,
    val dateTime: DateTime,
    val exercises: List<String>,
    val saved: Boolean,
    val archived: Boolean,
)
