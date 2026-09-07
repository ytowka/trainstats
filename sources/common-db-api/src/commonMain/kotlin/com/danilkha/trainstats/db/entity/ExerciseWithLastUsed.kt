package com.danilkha.trainstats.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ExerciseWithLastUsed(
    @Embedded val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseId",
        entity = ExerciseLastUsedView::class
    )
    val lastUsed: ExerciseLastUsedView?
)
