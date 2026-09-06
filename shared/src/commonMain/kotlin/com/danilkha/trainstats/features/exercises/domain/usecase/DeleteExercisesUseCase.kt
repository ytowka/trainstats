package com.danilkha.trainstats.features.exercises.domain.usecase

import com.danilkha.commoncore.usecase.UseCase
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository

class DeleteExercisesUseCase(
    private val exerciseRepository: ExerciseRepository
) : UseCase<String, Unit>(){
    override suspend fun execute(params: String) {
        exerciseRepository.deleteExercise(params)
    }
}