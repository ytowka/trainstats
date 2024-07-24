package com.danilkha.trainstats.features.workout.ui.history

import korlibs.time.DateTime

data class WorkoutHistoryModel(
    val id: Long,
    val date: DateTime,
    val exercises: List<String>
)