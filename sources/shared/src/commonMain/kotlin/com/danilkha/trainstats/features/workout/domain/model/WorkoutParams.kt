package com.danilkha.trainstats.features.workout.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class WorkoutParams(
    val id: String?,
    val date: LocalDate,
    val steps: List<SetParams>,
    val lastEdited: Instant? = null,
)