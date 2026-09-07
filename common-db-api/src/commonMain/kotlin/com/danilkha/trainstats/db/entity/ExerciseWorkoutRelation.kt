package com.danilkha.trainstats.db.entity

import androidx.room.TypeConverters
import kotlinx.datetime.Instant

@TypeConverters(RepetitionsDbTypeConverter::class)
data class ExerciseWorkoutRelation(

    val dateTime: Instant,
    val id: String = "",
    val workoutId: String,
    val exerciseId: String,
    val reps: RepetitionsDb,
    val weightKg: Float?,
    val orderPosition: Int,


    val exerciseName: String,
)
