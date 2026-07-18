package com.danilkha.trainstats.features.workout.ui.history

import kotlinx.datetime.Instant

data class WorkoutHistoryModel(
    val id: Long,
    val date: Instant,
    val exercises: List<String>
)