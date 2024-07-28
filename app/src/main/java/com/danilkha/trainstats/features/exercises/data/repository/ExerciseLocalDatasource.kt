package com.danilkha.trainstats.features.exercises.data.repository

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData

interface ExerciseLocalDatasource {

    suspend fun getAllExercises(): List<ExerciseData>
    suspend fun getExercise(id: Long): ExerciseData
    suspend fun getExerciseIds(names: List<String>): List<Pair<String, Long>>
    suspend fun findExercise(query: String): List<ExerciseData>
    suspend fun createExercise(exerciseData: ExerciseData): Long
    suspend fun updateExercise(exerciseData: ExerciseData)
    suspend fun deleteExercise(id: Long)
}