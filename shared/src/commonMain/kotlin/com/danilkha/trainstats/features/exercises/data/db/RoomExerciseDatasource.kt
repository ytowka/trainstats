package com.danilkha.trainstats.features.exercises.data.db

import com.danilkha.trainstats.core.utils.generateId
import com.danilkha.trainstats.features.exercises.data.repository.ExerciseLocalDatasource
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData

class RoomExerciseDatasource(
    private val exerciseDao: ExerciseDao
): ExerciseLocalDatasource{
    override suspend fun getAllExercises(): List<ExerciseData> {
        return exerciseDao.getAllExercises().map { it.toDomain() }
    }

    override suspend fun getExercise(id: String): ExerciseData {
        return exerciseDao.getExercise(id).toDomain()
    }

    override suspend fun getExerciseIds(names: List<String>): List<Pair<String, String>> {
        return exerciseDao.getExerciseIds(names).map {
            it.name to it.id
        }
    }

    override suspend fun findExercise(query: String): List<ExerciseData> {
        return exerciseDao.getAllExercises(query).map { it.toDomain() }
    }

    override suspend fun createExercise(exerciseData: ExerciseData): String {
        val id = exerciseData.id.ifEmpty { generateId() }
        exerciseDao.createExercise(exerciseData.copy(id = id).toEntity())
        return id
    }

    override suspend fun updateExercise(exerciseData: ExerciseData) {
        exerciseDao.updateExercise(exerciseData.toEntity())
    }

    override suspend fun deleteExercise(id: String) {
        exerciseDao.deleteExercise(id)
    }
}