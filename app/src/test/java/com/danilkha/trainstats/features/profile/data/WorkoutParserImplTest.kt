package com.danilkha.trainstats.features.profile.data

import com.danilkha.trainstats.features.exercises.data.FakeExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.profile.domain.ImportWorkoutsUseCase
import com.danilkha.trainstats.features.workout.data.FakeWorkoutRepository
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.io.File

class WorkoutParserImplTest {

    lateinit var workoutParserImpl: WorkoutParserImpl

    lateinit var importWorkoutsUseCase: ImportWorkoutsUseCase

    lateinit var exerciseRepository: ExerciseRepository
    lateinit var workoutRepository: WorkoutRepository

    @Before
    fun setup(){
        workoutParserImpl = WorkoutParserImpl()

        exerciseRepository = FakeExerciseRepository()
        workoutRepository = FakeWorkoutRepository(exerciseRepository)
        importWorkoutsUseCase = ImportWorkoutsUseCase(
            workoutParser = workoutParserImpl,
            workoutRepository = workoutRepository,
            exerciseRepository = exerciseRepository
        )
    }

    @Test
    fun testParse(){
        val file = File("workouts.txt")
        val string = String(file.inputStream().readAllBytes())
        val (exercises, params) = workoutParserImpl.parse(string)
        /*println(exercises.joinToString(separator = "\n", prefix = "${exercises.size} [", postfix = "]"))
        println()
        println(params.joinToString(separator = "\n", prefix = "${params.size} [", postfix = "]"))
        println("\n")
        println(exercises.map { it.name }.sorted().joinToString("\n"))*/
    }

    @Test
    fun testUseCase() = runBlocking{
        val file = File("workouts.txt")
        val string = String(file.inputStream().readAllBytes())
        importWorkoutsUseCase(ImportWorkoutsUseCase.Params(string))

        workoutRepository.getWorkoutHistory().first().forEach {
            val workout = workoutRepository.getWorkoutById(it.id)
            println(workout)
        }
        Unit
    }
}