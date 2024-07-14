package com.danilkha.trainstats.features.exercises.domain.usecase

import com.danilkha.trainstats.core.usecase.UseCase
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import javax.inject.Inject

class DeleteExercisesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) : UseCase<Long, Unit>(){
    override suspend fun execute(params: Long) {
        exerciseRepository.deleteExercise(params)
    }
}