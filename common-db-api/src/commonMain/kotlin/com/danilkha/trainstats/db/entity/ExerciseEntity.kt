package com.danilkha.trainstats.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [ Index(value = ["name"], unique = true) ]
)
data class ExerciseEntity(
    @PrimaryKey val id: String = "",
    val name: String,
    val imageUrl: String?,
    val separated: Boolean,
    val hasWeight: Boolean,
    val archived: Boolean,
){

    data class NameId(
        val id: String,
        val name: String,
    )
}
