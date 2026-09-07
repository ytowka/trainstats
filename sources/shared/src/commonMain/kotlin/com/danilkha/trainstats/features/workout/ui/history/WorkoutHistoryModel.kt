package com.danilkha.trainstats.features.workout.ui.history

import kotlinx.datetime.Instant

data class WorkoutHistoryModel(
    val id: String,
    val date: Instant,
    val exercises: List<String>
)