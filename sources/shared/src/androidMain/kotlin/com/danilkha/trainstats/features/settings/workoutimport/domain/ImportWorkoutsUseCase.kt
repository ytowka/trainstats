package com.danilkha.trainstats.features.settings.workoutimport.domain

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseVariation

import com.danilkha.trainstats.features.exercises.domain.usecase.CreateVariationUseCase
import android.net.Uri
import com.danilkha.commoncore.usecase.UseCase
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.settings.workoutimport.data.FileReader
import com.danilkha.trainstats.features.settings.workoutimport.data.ParserException
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.ExerciseSet
import com.danilkha.trainstats.features.workout.domain.model.Workout
import kotlin.time.Clock
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
import io.github.aakira.napier.Napier

class ImportWorkoutsUseCase(
    private val workoutParser: WorkoutParser,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val fileReader: FileReader,
    private val createVariationUseCase: CreateVariationUseCase,
): UseCase<ImportWorkoutsUseCase.Params, ImportWorkoutsUseCase.Result>(){

    override suspend fun execute(params: Params): Result {
        val textToParse = when(params) {
            is Params.File -> fileReader.readFile(params.uri)
            is Params.Text -> params.text
        }

        val (exercises, workouts) = workoutParser.parse(textToParse)

        val exerciseIds = mutableMapOf<String, String>()
        val exerciseNames = mutableMapOf<String, ExerciseData>()

        exercises.forEach { exercise ->
            val exerciseName = exercise.name.lowercase().trim()
            exerciseIds[exerciseName] = ""
            exerciseNames[exerciseName] = exercise
        }

        // Exact lookup includes archived exercises, as in the legacy import format.
        exerciseRepository.getExerciseIds(exerciseIds.keys.toList()).forEach { (name, id) ->
            exerciseIds[name.trim().lowercase()] = id
        }
        exerciseRepository.getAllExercises().forEach { exercise ->
            val name = exercise.name.trim().lowercase()
            if (exerciseIds[name] == "") exerciseIds[name] = exercise.id
        }
        val newExercises = exerciseIds.filterValues { it.isEmpty() }
        newExercises.forEach { (name, _) ->
            exerciseNames[name]?.let { exerciseIds[name] = exerciseRepository.createExercise(it) }
        }
        val variations = mutableMapOf<Pair<String, String>, ExerciseVariation>()
        exerciseIds.values.forEach { id ->
            exerciseRepository.getExercise(id).variations.forEach { variation ->
                variations[id to variation.name.trim().lowercase()] = variation
            }
        }
        workouts.flatMap { it.steps }.forEach { step ->
            val name = step.variationName?.trim()?.takeIf { it.isNotEmpty() } ?: return@forEach
            val exerciseId = exerciseIds.getValue(step.exerciseName.trim().lowercase())
            val key = exerciseId to name.lowercase()
            if (key !in variations) variations[key] = createVariationUseCase(CreateVariationUseCase.Params(exerciseId, name)).getOrThrow()
        }

        val timeZone = TimeZone.currentSystemDefault()
        workouts.map { workout ->
            val steps = workout.steps.mapIndexed { index, item ->
                val validName = item.exerciseName.lowercase().trim()
                val id = exerciseIds.getValue(validName)
                val variation = item.variationName?.let { variations[id to it.trim().lowercase()] }
                ExerciseSet(
                    id = "",
                    workoutId = "",
                    exerciseData = ExerciseData.stub(id, item.exerciseName),
                    reps = item.reps,
                    weight = item.weight,
                    orderPosition = index,
                    variationId = variation?.id,
                    variationName = variation?.name
                )
            }
            val now = Clock.System.now().toLocalDateTime(timeZone).time
            val dateTime = LocalDateTime(
                date = workout.date,
                time = now
            )
            Workout(
                id = "",
                dateTime = dateTime.toInstant(timeZone),
                steps = steps,
                saved = false,
                archived = false
            )
        }.forEach {
            workoutRepository.saveWorkout(it)
        }

        return Result(newExercises.size, workouts.size)
    }

    sealed interface Params {

        class Text(val text: String) : Params

        class File(val uri: Uri) : Params
    }
    data class Result(val exercises: Int, val workouts: Int)
}