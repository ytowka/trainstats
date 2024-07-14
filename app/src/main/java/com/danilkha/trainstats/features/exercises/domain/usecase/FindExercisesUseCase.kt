package com.danilkha.trainstats.features.exercises.domain.usecase

import com.danilkha.trainstats.core.usecase.SimpleUseCase
import com.danilkha.trainstats.core.usecase.UseCase
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.exercises.ui.ExerciseModel
import javax.inject.Inject

class FindExercisesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
): UseCase<String, List<ExerciseData>>(){

    override suspend fun execute(query: String): List<ExerciseData> {
        return exerciseRepository.findExercise(query)
    }
}