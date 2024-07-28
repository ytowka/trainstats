package com.danilkha.trainstats.features.exercises.data.repository

import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseLocalDatasource: ExerciseLocalDatasource,
) : ExerciseRepository{
    override suspend fun getAllExercises(): List<ExerciseData> {
        return withContext(Dispatchers.IO){
            exerciseLocalDatasource.getAllExercises()
        }

    }

    override suspend fun getExercise(id: Long): ExerciseData {
        return withContext(Dispatchers.IO){ exerciseLocalDatasource.getExercise(id) }
    }

    override suspend fun getExerciseIds(names: List<String>): List<Pair<String, Long>> {
        return exerciseLocalDatasource.getExerciseIds(names)
    }

    override suspend fun findExercise(query: String): List<ExerciseData> {
        return withContext(Dispatchers.IO){ exerciseLocalDatasource.findExercise(query) }
    }

    override suspend fun createExercise(exerciseData: ExerciseData): Long {
        return withContext(Dispatchers.IO){ exerciseLocalDatasource.createExercise(exerciseData) }
    }

    override suspend fun updateExercise(exerciseData: ExerciseData) {
        withContext(Dispatchers.IO){ exerciseLocalDatasource.updateExercise(exerciseData) }
    }

    override suspend fun deleteExercise(id: Long) {
        withContext(Dispatchers.IO){ exerciseLocalDatasource.deleteExercise(id) }
    }
}