package com.danilkha.trainstats.features.workout.data.db.entity.exercises

import androidx.room.TypeConverters
import com.danilkha.trainstats.features.workout.data.db.entity.RepetitionsDb
import com.danilkha.trainstats.features.workout.data.db.entity.RepetitionsDbTypeConverter
import kotlinx.datetime.Instant

@TypeConverters(RepetitionsDbTypeConverter::class)
data class ExerciseWorkoutRelation(

    val dateTime: Instant,
    val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val reps: RepetitionsDb,
    val weightKg: Float?,
    val orderPosition: Int,


    val exerciseName: String,
)