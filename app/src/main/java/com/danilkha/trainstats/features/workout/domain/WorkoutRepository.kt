package com.danilkha.trainstats.features.workout.domain

import com.danilkha.trainstats.features.workout.domain.model.ExerciseWorkout
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.trainstats.features.workout.domain.model.WorkoutPreview
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {

    fun getWorkoutHistory(): Flow<List<WorkoutPreview>>
    suspend fun getWorkoutById(id: Long): Workout
    suspend fun saveWorkout(workout: Workout): Long
    suspend fun commitWorkoutSave(id: Long)
    suspend fun archiveWorkout(id: Long)
    suspend fun deleteWorkout(id: Long)
    suspend fun getExerciseHistory(exerciseId: Long): List<ExerciseWorkout>
}