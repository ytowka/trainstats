package com.danilkha.trainstats.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = ExerciseEntity::class, parentColumns = ["id"], childColumns = ["exerciseId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index(value = ["exerciseId", "name"], unique = true)],
)
data class ExerciseVariationEntity(
    @PrimaryKey val id: String,
    val exerciseId: String,
    val name: String,
)
