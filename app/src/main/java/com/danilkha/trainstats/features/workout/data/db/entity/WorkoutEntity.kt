package com.danilkha.trainstats.features.workout.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.trainstats.features.workout.domain.model.WorkoutPreview
import korlibs.time.DateTime

@Entity
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateTime: DateTime,
    val saved: Boolean,
    val exercises: List<String>,
    val archived: Boolean,
)

fun WorkoutEntity.toPreview() =  WorkoutPreview(
    id = id,
    dateTime = dateTime,
    exercises = exercises,
    saved = saved,
    archived = archived
)

fun Workout.toEntity() = WorkoutEntity(
    id = id,
    dateTime = dateTime,
    saved = saved,
    exercises = steps.map {
        it.exerciseData.name
    }.toSet().toList(),
    archived = archived
)