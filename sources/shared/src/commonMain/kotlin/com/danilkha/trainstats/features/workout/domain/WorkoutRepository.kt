package com.danilkha.trainstats.features.workout.domain

import com.danilkha.trainstats.features.workout.domain.model.ExerciseWorkout
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.trainstats.features.workout.domain.model.WorkoutPreview
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {

    fun getWorkoutHistory(): Flow<List<WorkoutPreview>>
    suspend fun getWorkoutById(id: String): Workout
    suspend fun getAll() : List<Workout>
    suspend fun saveWorkout(workout: Workout): String
    suspend fun commitWorkoutSave(id: String)
    suspend fun archiveWorkout(id: String)
    suspend fun deleteWorkout(id: String)
    suspend fun getExerciseHistory(exerciseId: String): List<ExerciseWorkout>
}