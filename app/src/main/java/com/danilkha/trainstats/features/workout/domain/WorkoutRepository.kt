package com.danilkha.trainstats.features.workout.domain

import com.danilkha.trainstats.features.workout.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {

    fun getWorkoutHistory(): Flow<List<Workout>>
    suspend fun getWorkoutById(id: Long): Workout
    suspend fun saveWorkout(workout: Workout): Long
    suspend fun commitWorkoutSave(id: Long)
    suspend fun archiveWorkout(id: Long)
}