package com.danilkha.trainstats.features.workout.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.trainstats.features.workout.domain.model.WorkoutPreview
import kotlinx.datetime.Instant

@Entity
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateTime: Instant,
    val saved: Boolean,
    val exercises: List<String>,
    val archived: Boolean,
    val lastEdited: Instant? = null,
)

fun WorkoutEntity.toPreview() =  WorkoutPreview(
    id = id,
    dateTime = dateTime,
    exercises = exercises,
    saved = saved,
    archived = archived,
    lastEdited = lastEdited
)

fun Workout.toEntity() = WorkoutEntity(
    id = id,
    dateTime = dateTime,
    saved = saved,
    exercises = steps.map {
        it.exerciseData.name
    }.toSet().toList(),
    archived = archived,
    lastEdited = lastEdited
)