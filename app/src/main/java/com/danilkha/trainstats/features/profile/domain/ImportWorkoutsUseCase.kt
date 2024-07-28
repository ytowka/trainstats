package com.danilkha.trainstats.features.profile.domain

import android.util.Log
import com.danilkha.trainstats.core.usecase.UseCase
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.profile.data.ParserException
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.ExerciseSet
import com.danilkha.trainstats.features.workout.domain.model.Workout
import korlibs.time.DateTime
import javax.inject.Inject

class ImportWorkoutsUseCase @Inject constructor(
    private val workoutParser: WorkoutParser,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository
): UseCase<ImportWorkoutsUseCase.Params, ImportWorkoutsUseCase.Result>(){

    override suspend fun execute(params: Params): Result {
        val (exercises, workouts) = try{
            workoutParser.parse(params.text)
        }catch (e: ParserException){
            Log.d("debugg", "execute() called with: params = ${e.invalidLineIndex}")
            return Result.Error(e.invalidLineIndex)
        }

        val exerciseIds = mutableMapOf<String, Long>()
        val exerciseNames = mutableMapOf<String, ExerciseData>()

        exercises.forEach { exercise ->
            val exerciseName = exercise.name.lowercase().trim()
            exerciseIds[exerciseName] = 0
            exerciseNames[exerciseName] = exercise
        }

        val added = exerciseRepository.getExerciseIds(exerciseIds.keys.toList())
        added.forEach {
            exerciseIds[it.first] = it.second
        }
        val newExercises = exerciseIds.filter { (_, id) ->
            id == 0L
        }
        newExercises.forEach { (name, _) ->
            exerciseNames[name]?.let {
                val newId = exerciseRepository.createExercise(it)
                exerciseIds[name] = newId
            }
        }

        workouts.map { workout ->
            val steps = workout.steps.mapIndexed { index, item ->
                val validName = item.exerciseName.lowercase().trim()
                val id = exerciseIds[validName] ?: 0L
                ExerciseSet(
                    id = 0,
                    workoutId = 0,
                    exerciseData = ExerciseData.stub(id, item.exerciseName),
                    reps = item.reps,
                    weight = item.weight,
                    orderPosition = index
                )
            }
            val now =  DateTime.now()
            Workout(
                id = 0,
                dateTime = DateTime(
                    date = workout.date,
                    time = now.time
                ),
                steps = steps,
                saved = false,
                archived = false
            )
        }.forEach {
            workoutRepository.saveWorkout(it)
        }

        return Result.Success(newExercises.size, workouts.size)
    }

    class Params(val text: String)
    sealed interface Result{
        class Success(val exercises: Int, val workouts: Int): Result
        class Error(val invalidLine: Int): Result
    }
}