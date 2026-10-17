package com.danilkha.trainstats.features.exercises.data.db

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseVariation
import com.danilkha.commoncore.utils.generateId
import com.danilkha.trainstats.db.TrainStatsDb
import com.danilkha.trainstats.db.writeTransaction
import com.danilkha.trainstats.features.exercises.data.repository.ExerciseLocalDatasource
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData

class RoomExerciseDatasource(
    private val db: TrainStatsDb
): ExerciseLocalDatasource{
    private val exerciseDao = db.exerciseDao()

    override suspend fun getAllExercises(): List<ExerciseData> {
        return exerciseDao.getAllExercises().map { it.toDomain() }
    }

    override suspend fun getExercise(id: String): ExerciseData {
        return exerciseDao.getExercise(id).toDomain().copy(variations = exerciseDao.getVariations(id).map { it.toDomain() })
    }

    override suspend fun getExerciseIds(names: List<String>): List<Pair<String, String>> {
        return exerciseDao.getExerciseIds(names).map {
            it.name to it.id
        }
    }

    override suspend fun findExercise(query: String): List<ExerciseData> {
        return exerciseDao.getAllExercises(query).map { it.toDomain() }
    }

    override suspend fun createExercise(exerciseData: ExerciseData): String = db.writeTransaction {
        val id = exerciseData.id.ifEmpty { generateId() }
        val exercise = exerciseData.copy(id = id)
        exerciseDao.createExercise(exercise.toEntity())
        saveVariations(exercise)
        id
    }

    override suspend fun updateExercise(exerciseData: ExerciseData) = db.writeTransaction {
        exerciseDao.updateExercise(exerciseData.toEntity())
        saveVariations(exerciseData)
    }

    private suspend fun saveVariations(exercise: ExerciseData) {
        val names = exerciseDao.getVariations(exercise.id).map { it.name } + exercise.variations.map { it.name }
        var prefix = generateId()
        while (names.any { it.startsWith(prefix) }) prefix = generateId()
        // Release unique names so A -> B and B -> A can be saved together without deleting IDs.
        exerciseDao.prepareVariationsUpdate(exercise.id, prefix)
        exerciseDao.deleteMissingVariations(exercise.id, exercise.variations.map { it.id })
        exerciseDao.upsertVariations(exercise.variations.map { it.copy(exerciseId = exercise.id).toEntity() })
    }

    override suspend fun createVariation(exerciseId: String, name: String): ExerciseVariation {
        val variation = ExerciseVariation(generateId(), exerciseId, name)
        exerciseDao.insertVariation(variation.toEntity())
        return variation
    }

    override suspend fun deleteExercise(id: String) {
        exerciseDao.deleteExercise(id)
    }
}