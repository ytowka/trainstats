package com.danilkha.trainstats.features.exercises.domain.usecase

import com.danilkha.commoncore.usecase.UseCase
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import javax.inject.Inject

class GetAllExercisesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
): UseCase<String, List<ExerciseData>>(){

    override suspend fun execute(query: String): List<ExerciseData> {
        return if (query.isBlank()) {
            exerciseRepository.getAllExercises()
        } else {
            exerciseRepository.findExercise(query)
        }
    }
}