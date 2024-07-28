package com.danilkha.trainstats.features.exercises.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData

@Entity(
    indices = [ Index(value = ["name"], unique = true) ]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val imageUrl: String?,
    val separated: Boolean,
    val hasWeight: Boolean,
    val archived: Boolean,
){

    data class NameId(
        val id: Long,
        val name: String,
    )
}

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