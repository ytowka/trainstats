package com.danilkha.trainstats.features.exercises.data.repository

import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExerciseRepositoryImpl(
    private val exerciseLocalDatasource: ExerciseLocalDatasource,
) : ExerciseRepository{
    override suspend fun getAllExercises(): List<ExerciseData> {
        return withContext(Dispatchers.IO){
            exerciseLocalDatasource.getAllExercises()
        }

    }

    override suspend fun getExercise(id: String): ExerciseData {
        return withContext(Dispatchers.IO){ exerciseLocalDatasource.getExercise(id) }
    }

    override suspend fun getExerciseIds(names: List<String>): List<Pair<String, String>> {
        return exerciseLocalDatasource.getExerciseIds(names)
    }

    override suspend fun findExercise(query: String): List<ExerciseData> {
        return withContext(Dispatchers.IO){ exerciseLocalDatasource.findExercise(query) }
    }

    override suspend fun createExercise(exerciseData: ExerciseData): String {
        return withContext(Dispatchers.IO){ exerciseLocalDatasource.createExercise(exerciseData) }
    }

    override suspend fun updateExercise(exerciseData: ExerciseData) {
        withContext(Dispatchers.IO){ exerciseLocalDatasource.updateExercise(exerciseData) }
    }

    override suspend fun deleteExercise(id: String) {
        withContext(Dispatchers.IO){ exerciseLocalDatasource.deleteExercise(id) }
    }
}