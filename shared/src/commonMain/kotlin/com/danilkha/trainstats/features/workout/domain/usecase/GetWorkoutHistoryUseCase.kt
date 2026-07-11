package com.danilkha.trainstats.features.workout.domain.usecase

import com.danilkha.commoncore.usecase.SimpleFlowUseCase
import com.danilkha.commoncore.usecase.SimpleUseCase
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.trainstats.features.workout.domain.model.WorkoutPreview
import kotlinx.coroutines.flow.Flow

class GetWorkoutHistoryUseCase(
    private val workoutRepository: WorkoutRepository
) : SimpleFlowUseCase<List<WorkoutPreview>>(){

    override fun execute(): Flow<List<WorkoutPreview>> {
        return workoutRepository.getWorkoutHistory()
    }

}