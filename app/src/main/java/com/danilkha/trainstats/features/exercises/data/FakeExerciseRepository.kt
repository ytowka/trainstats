package com.danilkha.trainstats.features.exercises.data

import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import javax.inject.Inject

class FakeExerciseRepository @Inject constructor(): ExerciseRepository{

    var idSequence = 1L

    private val exercises = mutableListOf(
        ExerciseData(
            id = 1,
            name = "жим лежа",
            imageUrl = null,
            separated = false,
            hasWeight = true
        )
    )

    override suspend fun getAllExercises(): List<ExerciseData> {
       return exercises
    }

    override suspend fun getExercise(id: Long): ExerciseData {
        return exercises.first { it.id == id }
    }

    override suspend fun findExercise(query: String): List<ExerciseData> {
        return exercises.filter {
            it.name.contains(query)
        }
    }

    override suspend fun createExercise(exerciseData: ExerciseData) {
        idSequence++
        exercises.add(exerciseData.copy(id = idSequence))
    }

    override suspend fun updateExercise(exerciseData: ExerciseData) {
        val added = exercises.indexOfFirst { it.id == exerciseData.id }
        if(added != -1){
            exercises.removeAt(added)
            exercises.add(added, exerciseData)
        }
    }

    override suspend fun deleteExercise(id: Long) {
        val added = exercises.indexOfFirst { it.id == id }
        if(added != -1){
            exercises.removeAt(added)
        }
    }
}