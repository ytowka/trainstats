package com.danilkha.trainstats.features.workout.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.danilkha.trainstats.features.workout.domain.model.Workout

data class WorkoutWithExercises(
    @Embedded
    val workout: WorkoutEntity,

    @Relation(
        entity = ExerciseSetEntity::class,
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val exercises: List<ExerciseSetWithData>
) {
}

fun WorkoutWithExercises.toDomain() = Workout(
    id = workout.id,
    dateTime = workout.dateTime,
    steps = exercises.map {
        it.toDomain()
    },
    saved = workout.saved,
    archived = workout.archived
)