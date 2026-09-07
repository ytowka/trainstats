package com.danilkha.trainstats.features.workout.domain.usecase

import com.danilkha.commoncore.usecase.UseCase
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.ExerciseWorkout

class GetExerciseHistoryUseCase(
    private val workoutRepository: WorkoutRepository
) : UseCase<String, GetExerciseHistoryUseCase.Result>(){

    override suspend fun execute(params: String): Result {
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