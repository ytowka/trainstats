package com.danilkha.trainstats.features.exercises.data.db

import androidx.room.DatabaseView

@DatabaseView("""
    select s.exerciseId as exerciseId, count(*) as inWorkouts, count(distinct(w.id)) as totalSets from WorkoutEntity as w
    inner join ExerciseSetEntity as s on w.id = s.workoutId
    group by s.exerciseId
""", viewName = "ExerciseCountView")
data class ExerciseCountView(
    val exerciseId: Long,
    val inWorkouts: Int,
    val totalSets: Int
)