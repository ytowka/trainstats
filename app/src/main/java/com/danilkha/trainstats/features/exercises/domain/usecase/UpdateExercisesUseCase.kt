package com.danilkha.trainstats.features.exercises.domain.usecase

import com.danilkha.trainstats.core.usecase.UseCase
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import javax.inject.Inject

class UpdateExercisesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) : UseCase<ExerciseData, Unit>(){
    override suspend fun execute(params: ExerciseData) {
        exerciseRepository.updateExercise(params)
    }

}