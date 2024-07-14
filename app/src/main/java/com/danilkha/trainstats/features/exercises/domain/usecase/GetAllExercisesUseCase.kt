package com.danilkha.trainstats.features.exercises.domain.usecase

import com.danilkha.trainstats.core.usecase.SimpleUseCase
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.exercises.ui.ExerciseModel
import javax.inject.Inject

class GetAllExercisesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
): SimpleUseCase<List<ExerciseData>>(){

    override suspend fun execute(): List<ExerciseData> {
        return exerciseRepository.getAllExercises()
    }
}