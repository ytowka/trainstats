package com.danilkha.trainstats.features.workout.data.db

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.workout.data.db.entity.RepetitionsDb
import com.danilkha.trainstats.features.workout.data.db.entity.WorkoutEntity
import com.danilkha.trainstats.features.workout.data.db.entity.toDomain
import com.danilkha.trainstats.features.workout.data.db.entity.toEntity
import com.danilkha.trainstats.features.workout.data.db.entity.toPreview
import com.danilkha.trainstats.features.workout.data.repository.WorkoutLocalDatasource
import com.danilkha.trainstats.features.workout.domain.model.ExerciseSet
import com.danilkha.trainstats.features.workout.domain.model.ExerciseWorkout
import com.danilkha.trainstats.features.workout.domain.model.Kg
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

    override suspend fun deleteWorkout(id: Long) {
        workoutDao.deleteWorkout(id)
    }

    override suspend fun getExerciseHistory(exerciseId: Long): List<ExerciseWorkout> {
        val rawResult = workoutDao.getHistoryByExercise(exerciseId)
        val grouped = rawResult.groupBy { it.workoutId }
        return grouped.values.map { group ->
            val date = group.first().dateTime.date
            val sets = group.map { set ->
                val exerciseData = ExerciseData(
                    id = set.exerciseId,
                    name = set.exerciseName,
                    imageUrl = null,
                    separated = set.reps is RepetitionsDb.Double,
                    hasWeight = set.weightKg != null
                )
                ExerciseSet(
                    id = set.id,
                    workoutId = set.workoutId,
                    exerciseData = exerciseData,
                    reps = set.reps.toDomain(),
                    weight = set.weightKg?.let { Kg(it) },
                    orderPosition = set.orderPosition
                )
            }
            ExerciseWorkout(
                date = date,
                sets = sets
            )
        }
    }

}