package com.danilkha.trainstats.features.workout.domain.usecase

import com.danilkha.commoncore.usecase.UseCase
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.Workout

class GetWorkoutByIdUseCase(
    private val workoutRepository: WorkoutRepository
) : UseCase<Long, Workout>(){
    override suspend fun execute(params: Long): Workout {
        return workoutRepository.getWorkoutById(params)
    }
}