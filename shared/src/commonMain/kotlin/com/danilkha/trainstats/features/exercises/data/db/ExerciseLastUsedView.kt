package com.danilkha.trainstats.features.exercises.data.db

import androidx.room.DatabaseView
import kotlinx.datetime.Instant

@DatabaseView("""
    SELECT s.exerciseId as exerciseId, MAX(w.dateTime) as lastUsed 
    FROM WorkoutEntity w
    INNER JOIN ExerciseSetEntity s ON w.id = s.workoutId
    GROUP BY s.exerciseId
""", viewName = "ExerciseLastUsedView")
data class ExerciseLastUsedView(
    val exerciseId: String,
    val lastUsed: Instant
)
