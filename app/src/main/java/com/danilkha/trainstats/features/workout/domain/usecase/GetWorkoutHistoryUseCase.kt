package com.danilkha.trainstats.features.workout.domain.usecase

import com.danilkha.trainstats.core.usecase.SimpleFlowUseCase
import com.danilkha.trainstats.core.usecase.SimpleUseCase
import com.danilkha.trainstats.features.workout.data.FakeWorkoutRepository
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.trainstats.features.workout.domain.model.WorkoutPreview
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWorkoutHistoryUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : SimpleFlowUseCase<List<WorkoutPreview>>(){

    override fun execute(): Flow<List<WorkoutPreview>> {
        return workoutRepository.getWorkoutHistory()
    }

}