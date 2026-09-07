package com.danilkha.trainstats.features.workout.domain.model

import kotlinx.datetime.Instant

data class WorkoutPreview(
    val id: String = "",
    val dateTime: Instant,
    val exercises: List<String>,
    val saved: Boolean,
    val archived: Boolean,
    val lastEdited: Instant? = null,
)
