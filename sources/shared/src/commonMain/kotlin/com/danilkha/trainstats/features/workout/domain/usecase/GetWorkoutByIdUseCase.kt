package com.danilkha.trainstats.features.workout.domain.usecase

import com.danilkha.commoncore.usecase.UseCase
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.Workout

class GetWorkoutByIdUseCase(
    private val workoutRepository: WorkoutRepository
) : UseCase<String, Workout>(){
    override suspend fun execute(params: String): Workout {
        return workoutRepository.getWorkoutById(params)
    }
}