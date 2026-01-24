package com.danilkha.trainstats.features.workout.domain.usecase

import android.util.Log
import com.danilkha.trainstats.core.usecase.UseCase
import com.danilkha.trainstats.core.utils.toLocal
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.ExerciseSet
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.trainstats.features.workout.domain.model.WorkoutParams
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class SaveWorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : UseCase<WorkoutParams, Long>(){


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
        val now = Clock.System.now().toLocal().time
        val dateTime = LocalDateTime(
            date = params.date,
            time = now
        )
        val workout = Workout(
            id = params.id ?: 0,
            dateTime = dateTime.toInstant(TimeZone.currentSystemDefault()),
            steps = steps,
            saved = false,
            archived = false,
            lastEdited = params.lastEdited
        )
        return workoutRepository.saveWorkout(workout)
    }

}