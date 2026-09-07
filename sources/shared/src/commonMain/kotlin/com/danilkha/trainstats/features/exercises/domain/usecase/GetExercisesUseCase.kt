package com.danilkha.trainstats.features.exercises.domain.usecase

import com.danilkha.commoncore.usecase.SimpleUseCase
import com.danilkha.commoncore.usecase.UseCase
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData

class GetExercisesUseCase(
    private val exerciseRepository: ExerciseRepository
): UseCase<String, ExerciseData>(){

    override suspend fun execute(id: String): ExerciseData {
        return exerciseRepository.getExercise(id)
    }
}