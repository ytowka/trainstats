package com.danilkha.trainstats.features.workout.ui

import androidx.compose.runtime.Immutable
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.workout.domain.model.ExerciseSet
import com.danilkha.trainstats.features.workout.domain.model.Kg
import com.danilkha.trainstats.features.workout.domain.model.Repetitions
import korlibs.time.DateTime

class WorkoutModel(
    val id: Long,
    val dateTime: DateTime,
    val groups: List<ExerciseGroup>,
)

class ExerciseGroup(
    val exerciseId: Long = 0,
    val name: String,
    val imageUrl: String?,
    val sets: List<ExerciseSetModel>,
)

class ExerciseSetModel(
    val tempId: Long,
    val dateTime: DateTime,
    val reps: RepetitionsModel,
    val weight: Kg?,
)

@Immutable
sealed interface RepetitionsModel{

    data class Single(val reps: Float) : RepetitionsModel
    data class Double(val left: Float, val right: Float) : RepetitionsModel
}

enum class Side { Left, Right }
