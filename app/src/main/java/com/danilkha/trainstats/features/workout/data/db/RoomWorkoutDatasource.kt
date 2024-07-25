package com.danilkha.trainstats.features.workout.data.db

import com.danilkha.trainstats.features.workout.data.db.entity.WorkoutEntity
import com.danilkha.trainstats.features.workout.data.db.entity.toDomain
import com.danilkha.trainstats.features.workout.data.db.entity.toEntity
import com.danilkha.trainstats.features.workout.data.db.entity.toPreview
import com.danilkha.trainstats.features.workout.data.repository.WorkoutLocalDatasource
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.trainstats.features.workout.domain.model.WorkoutPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomWorkoutDatasource @Inject constructor(
    private val workoutDao: WorkoutDao
): WorkoutLocalDatasource {
    override fun getWorkoutHistory(): Flow<List<WorkoutPreview>> {
        return workoutDao.getWorkoutHistory().map {
            it.map(WorkoutEntity::toPreview)
        }
    }

    override suspend fun getWorkoutById(id: Long): Workout {
        return workoutDao.getWorkoutById(id).toDomain()
    }

    override suspend fun saveWorkout(workout: Workout): Long {
        return workoutDao.updateWorkout(
            workout.toEntity(),
            workout.steps.map { it.toEntity() }
        )
    }

    override suspend fun commitWorkoutSave(id: Long) {
        workoutDao.commitWorkoutSave(id)
    }

    override suspend fun archiveWorkout(id: Long) {
        workoutDao.archiveWorkout(id)
    }

}