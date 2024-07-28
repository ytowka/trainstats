package com.danilkha.trainstats.features.profile.domain

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.workout.domain.model.WorkoutParams

interface WorkoutParser {
    fun parse(text: String): Pair<List<ExerciseData>,List<WorkoutParams>>
}