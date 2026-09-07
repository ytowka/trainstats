package com.danilkha.trainstats.db.entity

import androidx.room.Embedded
import androidx.room.Relation

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
