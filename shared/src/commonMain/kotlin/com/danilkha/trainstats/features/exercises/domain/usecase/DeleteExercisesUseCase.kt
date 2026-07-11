package com.danilkha.trainstats.features.exercises.domain.usecase

import com.danilkha.commoncore.usecase.UseCase
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository

class DeleteExercisesUseCase(
    private val exerciseRepository: ExerciseRepository
) : UseCase<Long, Unit>(){
    override suspend fun execute(params: Long) {
        exerciseRepository.deleteExercise(params)
    }
}