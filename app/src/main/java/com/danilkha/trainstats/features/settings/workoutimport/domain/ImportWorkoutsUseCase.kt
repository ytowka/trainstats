package com.danilkha.trainstats.features.settings.workoutimport.domain

import android.net.Uri
import android.util.Log
import com.danilkha.commoncore.usecase.UseCase
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.settings.workoutimport.data.FileReader
import com.danilkha.trainstats.features.settings.workoutimport.data.ParserException
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.ExerciseSet
import com.danilkha.trainstats.features.workout.domain.model.Workout
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class ImportWorkoutsUseCase @Inject constructor(
    private val workoutParser: WorkoutParser,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val fileReader: FileReader,
): UseCase<ImportWorkoutsUseCase.Params, ImportWorkoutsUseCase.Result>(){

    override suspend fun execute(params: Params): Result {
        val textToParse = when(params) {
            is Params.File -> fileReader.readFile(params.uri)
            is Params.Text -> params.text
        }

        val (exercises, workouts) = try{
            workoutParser.parse(textToParse)
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

        val timeZone = TimeZone.currentSystemDefault()
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
            val now = Clock.System.now().toLocalDateTime(timeZone).time
            val dateTime = LocalDateTime(
                date = workout.date,
                time = now
            )
            Workout(
                id = 0,
                dateTime = dateTime.toInstant(timeZone),
                steps = steps,
                saved = false,
                archived = false
            )
        }.forEach {
            workoutRepository.saveWorkout(it)
        }

        return Result.Success(newExercises.size, workouts.size)
    }

    sealed interface Params {

        class Text(val text: String) : Params

        class File(val uri: Uri) : Params
    }
    sealed interface Result{
        class Success(val exercises: Int, val workouts: Int): Result
        class Error(val invalidLine: Int): Result
    }
}