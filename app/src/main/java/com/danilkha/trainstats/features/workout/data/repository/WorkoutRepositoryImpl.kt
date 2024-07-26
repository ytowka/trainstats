package com.danilkha.trainstats.features.workout.data.repository

import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.trainstats.features.workout.domain.model.WorkoutPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val workoutLocalDatasource: WorkoutLocalDatasource,
) : WorkoutRepository{
    override fun getWorkoutHistory(): Flow<List<WorkoutPreview>> {
        return workoutLocalDatasource.getWorkoutHistory()
    }

    override suspend fun getWorkoutById(id: Long): Workout {
        return withContext(Dispatchers.IO){ workoutLocalDatasource.getWorkoutById(id) }
    }

    override suspend fun saveWorkout(workout: Workout): Long {
        return withContext(Dispatchers.IO){ workoutLocalDatasource.saveWorkout(workout) }
    }

    override suspend fun commitWorkoutSave(id: Long) {
        withContext(Dispatchers.IO){ workoutLocalDatasource.commitWorkoutSave(id) }
    }

    override suspend fun archiveWorkout(id: Long) {
        withContext(Dispatchers.IO){ workoutLocalDatasource.archiveWorkout(id) }
    }

    override suspend fun deleteWorkout(id: Long) {
        withContext(Dispatchers.IO){ workoutLocalDatasource.deleteWorkout(id) }
    }
}