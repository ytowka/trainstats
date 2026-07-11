package com.danilkha.trainstats.features.workout.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.danilkha.trainstats.features.exercises.data.db.ExerciseEntity
import com.danilkha.trainstats.features.workout.domain.model.ExerciseSet

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
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val reps: RepetitionsDb,
    val weightKg: Float?,
    val orderPosition: Int,
)

fun ExerciseSet.toEntity() =  ExerciseSetEntity(
    id = id,
    workoutId = workoutId,
    exerciseId = exerciseData.id,
    reps = reps.toEntity(),
    weightKg = weight?.value,
    orderPosition = orderPosition
)