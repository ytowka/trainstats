package com.danilkha.trainstats.features.exercises.domain.usecase

import com.danilkha.commoncore.usecase.SimpleUseCase
import com.danilkha.commoncore.usecase.UseCase
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.exercises.ui.ExerciseModel
import javax.inject.Inject

class GetExercisesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
): UseCase<Long, ExerciseData>(){

    override suspend fun execute(id: Long): ExerciseData {
        return exerciseRepository.getExercise(id)
    }
}