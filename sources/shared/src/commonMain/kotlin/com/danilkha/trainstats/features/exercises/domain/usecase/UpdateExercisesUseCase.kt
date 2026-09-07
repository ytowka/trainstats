package com.danilkha.trainstats.features.exercises.domain.usecase

import com.danilkha.commoncore.usecase.UseCase
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData

class UpdateExercisesUseCase(
    private val exerciseRepository: ExerciseRepository,
) : UseCase<ExerciseData, Unit>(){
    override suspend fun execute(params: ExerciseData) {
        exerciseRepository.updateExercise(params)
    }

}