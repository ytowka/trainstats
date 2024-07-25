package com.danilkha.trainstats.features.workout.domain.usecase

import com.danilkha.trainstats.core.usecase.UseCase
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.ExerciseSet
import com.danilkha.trainstats.features.workout.domain.model.Kg
import com.danilkha.trainstats.features.workout.domain.model.Repetitions
import com.danilkha.trainstats.features.workout.domain.model.Workout
import korlibs.time.Date
import korlibs.time.DateTime
import javax.inject.Inject

class SaveWorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : UseCase<SaveWorkoutUseCase.WorkoutParams, Long>(){


    override suspend fun execute(params: WorkoutParams): Long {
        val steps = params.steps.mapIndexed { index, item ->
            ExerciseSet(
                id = 0,
                workoutId = 0,
                exerciseData = ExerciseData.stub(item.exerciseId, item.exerciseName),
                reps = item.reps,
                weight = item.weight,
                orderPosition = index
            )
        }
        val now =  DateTime.now()
        val workout = Workout(
            id = params.id ?: 0,
            dateTime = DateTime(
                date = params.date,
                time = now.time
            ),
            steps = steps,
            saved = false,
            archived = false
        )
        return workoutRepository.saveWorkout(workout)
    }

    class WorkoutParams(
        val id: Long?,
        val date: Date,
        val steps: List<SetParams>,
    )

    class SetParams(
        val exerciseId: Long,
        val exerciseName: String,
        val reps: Repetitions,
        val weight: Kg?,
    )
}