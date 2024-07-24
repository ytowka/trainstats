package com.danilkha.trainstats.features.workout.domain.usecase

import com.danilkha.trainstats.core.usecase.UseCase
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.Workout
import javax.inject.Inject

class GetWorkoutByIdUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : UseCase<Long, Workout>(){
    override suspend fun execute(params: Long): Workout {
        return workoutRepository.getWorkoutById(params)
    }
}