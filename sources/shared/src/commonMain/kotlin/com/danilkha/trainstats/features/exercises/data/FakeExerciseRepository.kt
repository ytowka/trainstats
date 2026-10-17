package com.danilkha.trainstats.features.exercises.data

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseVariation
import com.danilkha.commoncore.utils.generateId
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData

class FakeExerciseRepository() : ExerciseRepository{

    private val exercises = mutableListOf<ExerciseData>()

    fun prepopulate() {
        exercises.add(ExerciseData(
            id = generateId(),
            name = "жим лежа",
            imageUrl = null,
            separated = false,
            hasWeight = true
        ))
    }

    override suspend fun getAllExercises(): List<ExerciseData> {
       return exercises
    }

    override suspend fun getExercise(id: String): ExerciseData {
        return exercises.first { it.id == id }
    }

    override suspend fun getExerciseIds(names: List<String>): List<Pair<String, String>> {
        return exercises.filter { it.name in names }
            .map { it.name to it.id }
    }

    override suspend fun findExercise(query: String): List<ExerciseData> {
        return exercises.filter {
            it.name.contains(query)
        }
    }

    override suspend fun createExercise(exerciseData: ExerciseData): String {
        val id = exerciseData.id.ifEmpty { generateId() }
        exercises.add(exerciseData.copy(id = id, variations = exerciseData.variations.map { it.copy(exerciseId = id) }))
        return id
    }

    override suspend fun updateExercise(exerciseData: ExerciseData) {
        val added = exercises.indexOfFirst { it.id == exerciseData.id }
        if(added != -1){
            exercises.removeAt(added)
            exercises.add(added, exerciseData)
        }
    }

    override suspend fun createVariation(exerciseId: String, name: String): ExerciseVariation {
        val exercise = getExercise(exerciseId)
        require(name.isNotBlank() && exercise.variations.none { it.name.trim().lowercase() == name.trim().lowercase() })
        val variation = ExerciseVariation(generateId(), exerciseId, name)
        updateExercise(exercise.copy(variations = exercise.variations + variation))
        return variation
    }

    override suspend fun deleteExercise(id: String) {
        val added = exercises.indexOfFirst { it.id == id }
        if(added != -1){
            exercises.removeAt(added)
        }
    }
}