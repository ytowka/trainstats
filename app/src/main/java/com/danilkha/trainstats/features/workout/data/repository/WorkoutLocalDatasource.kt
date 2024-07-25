package com.danilkha.trainstats.features.workout.data.repository

import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.trainstats.features.workout.domain.model.WorkoutPreview
import kotlinx.coroutines.flow.Flow

interface WorkoutLocalDatasource {
    fun getWorkoutHistory(): Flow<List<WorkoutPreview>>
    suspend fun getWorkoutById(id: Long): Workout
    suspend fun saveWorkout(workout: Workout): Long
    suspend fun commitWorkoutSave(id: Long)
    suspend fun archiveWorkout(id: Long)
}