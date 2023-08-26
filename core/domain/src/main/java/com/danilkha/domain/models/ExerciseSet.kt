package com.danilkha.domain.models

import korlibs.time.DateTime
import korlibs.time.TimeSpan

sealed interface Step{
    data class ExerciseSet(
        val id: Long = 0,
        val workoutId: Long,
        val dateTime: DateTime,
        val duration: TimeSpan,
        val exerciseData: ExerciseData,
        val reps: Int,
    ) : Step

    data class Rest(
        val id: Long = 0,
        val time: TimeSpan,
        val actualTime: TimeSpan,
    ) : Step
}

