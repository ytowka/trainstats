package com.danilkha.trainstats.features.workout.data.db.entity.exercises

import androidx.room.Embedded
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverters
import com.danilkha.trainstats.features.exercises.data.db.ExerciseEntity
import com.danilkha.trainstats.features.workout.data.db.entity.ExerciseSetEntity
import com.danilkha.trainstats.features.workout.data.db.entity.RepetitionsDb
import com.danilkha.trainstats.features.workout.data.db.entity.RepetitionsDbTypeConverter
import korlibs.time.Date
import korlibs.time.DateTime

@TypeConverters(RepetitionsDbTypeConverter::class)
data class ExerciseWorkoutRelation(

    val dateTime: DateTime,
    val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val reps: RepetitionsDb,
    val weightKg: Float?,
    val orderPosition: Int,


    val exerciseName: String,
)