package com.danilkha.trainstats.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity
data class WorkoutEntity(
    @PrimaryKey
    val id: String = "",
    val dateTime: Instant,
    val saved: Boolean,
    val exercises: List<String>,
    val archived: Boolean,
    val lastEdited: Instant? = null,
)
