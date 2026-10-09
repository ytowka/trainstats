package com.danilkha.trainstats.features.stats.domain

import com.danilkha.commoncore.usecase.UseCase
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository

class GetStatisticsUseCase(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
) : UseCase<Unit, StatisticsData>() {
    override suspend fun execute(params: Unit): StatisticsData = StatisticsData(
        // Preserve the same usage ranking supplied to the exercise list.
        exercises = exerciseRepository.getAllExercises(),
        workouts = workoutRepository.getAll().filter { !it.archived && it.steps.isNotEmpty() },
    )
}
