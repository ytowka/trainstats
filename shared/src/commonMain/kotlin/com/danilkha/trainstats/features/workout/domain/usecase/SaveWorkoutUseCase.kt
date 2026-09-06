package com.danilkha.trainstats.features.workout.domain.usecase

import com.danilkha.commoncore.usecase.UseCase
import com.danilkha.commoncore.utils.toLocal
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.ExerciseSet
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.trainstats.features.workout.domain.model.WorkoutParams
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class SaveWorkoutUseCase(
    private val workoutRepository: WorkoutRepository
) : UseCase<WorkoutParams, String>(){


    override suspend fun execute(params: WorkoutParams): String {
        val steps = params.steps.mapIndexed { index, item ->
            ExerciseSet(
                id = "",
                workoutId = "",
                exerciseData = ExerciseData.stub(item.exerciseId, item.exerciseName),
                reps = item.reps,
                weight = item.weight,
                orderPosition = index
            )
        }
        val now = Clock.System.now().toLocal().time
        val dateTime = LocalDateTime(
            date = params.date,
            time = now
        )
        val workout = Workout(
            id = params.id ?: "",
            dateTime = dateTime.toInstant(TimeZone.currentSystemDefault()),
            steps = steps,
            saved = false,
            archived = false,
            lastEdited = params.lastEdited
        )
        return workoutRepository.saveWorkout(workout)
    }

}