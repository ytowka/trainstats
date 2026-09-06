package com.danilkha.trainstats.features.exercises.data.repository

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData

interface ExerciseLocalDatasource {

    suspend fun getAllExercises(): List<ExerciseData>
    suspend fun getExercise(id: String): ExerciseData
    suspend fun getExerciseIds(names: List<String>): List<Pair<String, String>>
    suspend fun findExercise(query: String): List<ExerciseData>
    suspend fun createExercise(exerciseData: ExerciseData): String
    suspend fun updateExercise(exerciseData: ExerciseData)
    suspend fun deleteExercise(id: String)
}