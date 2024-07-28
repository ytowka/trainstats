package com.danilkha.trainstats.features.exercises.data.db

import com.danilkha.trainstats.features.exercises.data.repository.ExerciseLocalDatasource
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import javax.inject.Inject

class RoomExerciseDatasource @Inject constructor(
    private val exerciseDao: ExerciseDao
): ExerciseLocalDatasource{
    override suspend fun getAllExercises(): List<ExerciseData> {
        return exerciseDao.getAllExercises().map(ExerciseEntity::toDomain)
    }

    override suspend fun getExercise(id: Long): ExerciseData {
        return exerciseDao.getExercise(id).toDomain()
    }

    override suspend fun getExerciseIds(names: List<String>): List<Pair<String, Long>> {
        return exerciseDao.getExerciseIds(names).map {
            it.name to it.id
        }
    }

    override suspend fun findExercise(query: String): List<ExerciseData> {
        return exerciseDao.findExercise(query).map {
            it.toDomain()
        }
    }

    override suspend fun createExercise(exerciseData: ExerciseData): Long {
        return exerciseDao.createExercise(exerciseData.toEntity())
    }

    override suspend fun updateExercise(exerciseData: ExerciseData) {
        exerciseDao.updateExercise(exerciseData.toEntity())
    }

    override suspend fun deleteExercise(id: Long) {
        exerciseDao.deleteExercise(id)
    }
}