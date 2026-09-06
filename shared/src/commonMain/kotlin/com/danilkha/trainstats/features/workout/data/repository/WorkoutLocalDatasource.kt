package com.danilkha.trainstats.features.workout.data.repository

import com.danilkha.trainstats.features.workout.domain.model.ExerciseWorkout
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.trainstats.features.workout.domain.model.WorkoutPreview
import kotlinx.coroutines.flow.Flow

interface WorkoutLocalDatasource {
    fun getWorkoutHistory(): Flow<List<WorkoutPreview>>
    suspend fun getAll(): List<Workout>
    suspend fun getWorkoutById(id: String): Workout
    suspend fun saveWorkout(workout: Workout): String
    suspend fun commitWorkoutSave(id: String)
    suspend fun archiveWorkout(id: String)
    suspend fun deleteWorkout(id: String)
    suspend fun getExerciseHistory(exerciseId: String): List<ExerciseWorkout>
}