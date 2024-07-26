package com.danilkha.trainstats.features.exercises.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData

@Entity
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val imageUrl: String?,
    val separated: Boolean,
    val hasWeight: Boolean,
    val archived: Boolean,
)

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