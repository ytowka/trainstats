package com.danilkha.trainstats.features.exercises.data.db

import androidx.room.Embedded
import androidx.room.Relation
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData

data class ExerciseWithLastUsed(
    @Embedded val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseId",
        entity = ExerciseLastUsedView::class
    )
    val lastUsed: ExerciseLastUsedView?
)

fun ExerciseWithLastUsed.toDomain() = ExerciseData(
    id = exercise.id,
    name = exercise.name,
    imageUrl = exercise.imageUrl,
    separated = exercise.separated,
    hasWeight = exercise.hasWeight,
    lastUsedDate = lastUsed?.lastUsed
)
