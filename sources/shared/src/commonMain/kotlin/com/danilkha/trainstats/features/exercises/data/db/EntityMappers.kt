package com.danilkha.trainstats.features.exercises.data.db

import com.danilkha.trainstats.db.entity.ExerciseEntity
import com.danilkha.trainstats.db.entity.ExerciseWithLastUsed
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData

fun  ExerciseEntity.toDomain() = ExerciseData(
    id = id,
    name = name,
    imageUrl = imageUrl,
    separated = separated,
    hasWeight = hasWeight
)

fun ExerciseData.toEntity() = ExerciseEntity(
    id = id,
    name = name,
    imageUrl = imageUrl,
    separated = separated,
    hasWeight = hasWeight,
    archived = false
)

fun ExerciseWithLastUsed.toDomain() = ExerciseData(
    id = exercise.id,
    name = exercise.name,
    imageUrl = exercise.imageUrl,
    separated = exercise.separated,
    hasWeight = exercise.hasWeight,
    lastUsedDate = lastUsed?.lastUsed
)
