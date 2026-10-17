package com.danilkha.trainstats.features.workout.domain.usecase

import com.danilkha.commoncore.usecase.UseCase
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.ExerciseWorkout

class GetExerciseHistoryUseCase(
    private val workoutRepository: WorkoutRepository
) : UseCase<String, GetExerciseHistoryUseCase.Result>(){

    override suspend fun execute(params: String): Result {
        val workouts = workoutRepository.getExerciseHistory(params)

        val exerciseName = workouts.firstOrNull()?.sets?.firstOrNull()?.exerciseData?.name.orEmpty()
        val exercises = workouts.map { workout ->
            ExerciseWorkout(
                date = workout.date,
                sets = workout.sets.sortedBy { it.orderPosition }
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