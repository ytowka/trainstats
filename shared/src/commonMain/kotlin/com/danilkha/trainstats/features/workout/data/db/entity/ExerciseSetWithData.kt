package com.danilkha.trainstats.features.workout.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.danilkha.trainstats.features.exercises.data.db.ExerciseEntity
import com.danilkha.trainstats.features.exercises.data.db.toDomain
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.workout.domain.model.ExerciseSet
import com.danilkha.trainstats.features.workout.domain.model.Kg

data class ExerciseSetWithData(
    @Embedded
    val setEntity: ExerciseSetEntity,

    @Relation(
        entity = ExerciseEntity::class,
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity
)

fun ExerciseSetWithData.toDomain() = ExerciseSet(
    id = setEntity.id,
    workoutId = setEntity.workoutId,
    exerciseData = exercise.toDomain(),
    reps = setEntity.reps.toDomain(),
    weight = setEntity.weightKg?.let { Kg(it) },
    orderPosition = setEntity.orderPosition
)