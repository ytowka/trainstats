package com.danilkha.domain.models

import korlibs.time.DateTime

data class Workout(
    val id: Long = 0,
    val group: WorkoutGroup,
    val dateTime: DateTime,
    val steps: List<Step>
)
