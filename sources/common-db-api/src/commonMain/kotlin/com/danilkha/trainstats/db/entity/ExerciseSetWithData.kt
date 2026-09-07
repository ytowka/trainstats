package com.danilkha.trainstats.db.entity

import androidx.room.Embedded
import androidx.room.Relation

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
