package com.danilkha.trainstats.features.workout.domain.usecase

import com.danilkha.trainstats.core.usecase.UseCase
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.ExerciseWorkout
import javax.inject.Inject

class GetExerciseHistoryUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : UseCase<Long, GetExerciseHistoryUseCase.Result>(){

    override suspend fun execute(params: Long): Result {
        val workouts = workoutRepository.getExerciseHistory(params)

        val exerciseName = workouts.first().sets.first().exerciseData.name
        val exercises = workouts.map { workout ->
            ExerciseWorkout(
                date = workout.date,
                sets = workout.sets
            )
        }
        return Result(
            exerciseName = exerciseName,
            exercises = exercises
        )
    }

    class Result(
        val exerciseName: String,
        val exercises: List<ExerciseWorkout>
    )
}