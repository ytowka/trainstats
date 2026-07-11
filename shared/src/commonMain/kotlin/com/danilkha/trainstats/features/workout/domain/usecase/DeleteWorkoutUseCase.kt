package com.danilkha.trainstats.features.workout.domain.usecase

import com.danilkha.commoncore.usecase.UseCase
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository

class DeleteWorkoutUseCase(
    private val workoutRepository: WorkoutRepository
): UseCase<Long, Unit>(){
    override suspend fun execute(params: Long) {
        workoutRepository.deleteWorkout(params)
    }
}