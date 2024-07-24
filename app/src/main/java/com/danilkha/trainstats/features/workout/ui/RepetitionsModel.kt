package com.danilkha.trainstats.features.workout.ui

import androidx.compose.runtime.Immutable
import com.danilkha.trainstats.features.workout.domain.model.Repetitions

@Immutable
sealed interface RepetitionsModel{
    data class Single(val reps: Float?) : RepetitionsModel
    data class Double(val left: Float?, val right: Float?) : RepetitionsModel
}

fun Repetitions.toModel() = when(this){
    is Repetitions.Double -> RepetitionsModel.Double(left, right)
    is Repetitions.Single -> RepetitionsModel.Single(reps)
}

fun RepetitionsModel.toDomain() = when(this){
    is RepetitionsModel.Double -> Repetitions.Double(left ?: 0f, right ?: 0f)
    is RepetitionsModel.Single -> Repetitions.Single(reps ?: 0f)
}

