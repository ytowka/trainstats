package com.danilkha.trainstats.features.workout.domain.usecase

import com.danilkha.trainstats.core.usecase.UseCase
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import javax.inject.Inject

class ArchiveWorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : UseCase<Long, Unit>(){

    override suspend fun execute(params: Long) {
        workoutRepository.archiveWorkout(params)
    }
}