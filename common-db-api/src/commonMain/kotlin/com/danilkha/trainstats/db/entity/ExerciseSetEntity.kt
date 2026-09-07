package com.danilkha.trainstats.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT
        ),
    ],
    indices = [
        Index("workoutId"),
        Index("exerciseId"),
    ]
)
@TypeConverters(RepetitionsDbTypeConverter::class)
data class ExerciseSetEntity(
    @PrimaryKey
    val id: String = "",
    val workoutId: String,
    val exerciseId: String,
    val reps: RepetitionsDb,
    val weightKg: Float?,
    val orderPosition: Int,
)
