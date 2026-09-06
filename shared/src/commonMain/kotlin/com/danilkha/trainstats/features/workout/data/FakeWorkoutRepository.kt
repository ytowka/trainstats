package com.danilkha.trainstats.features.workout.data

import com.danilkha.trainstats.core.utils.generateId
import com.danilkha.trainstats.features.exercises.data.FakeExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.ExerciseSet
import com.danilkha.trainstats.features.workout.domain.model.ExerciseWorkout
import com.danilkha.trainstats.features.workout.domain.model.Kg
import com.danilkha.trainstats.features.workout.domain.model.Repetitions
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.trainstats.features.workout.domain.model.WorkoutPreview
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

class FakeWorkoutRepository(
    private val fakeExerciseRepository: ExerciseRepository,
): WorkoutRepository {

    private val workouts = MutableStateFlow(mapOf<String, Workout>())

    fun prepopulate() {
        val id = generateId()
        val exerciseData1 = runBlocking{ fakeExerciseRepository.getAllExercises().firstOrNull() }
        val steps = exerciseData1?.let {
            buildList {
                add(ExerciseSet(
                    id = generateId(),
                    workoutId = id,
                    exerciseData = exerciseData1,
                    reps = Repetitions.Single(10f),
                    weight = Kg(50f),
                    orderPosition = 0,
                )
                )
                add(
                    ExerciseSet(
                        id = generateId(),
                        workoutId = id,
                        exerciseData = exerciseData1,
                        reps = Repetitions.Single(10f),
                        weight = Kg(50f),
                        orderPosition = 0
                    )
                )
                add(
                    ExerciseSet(
                        id = generateId(),
                        workoutId = id,
                        exerciseData = exerciseData1,
                        reps = Repetitions.Single(10f),
                        weight = Kg(50f),
                        orderPosition = 0
                    )
                )
            }
        } ?: emptyList()

        val initWorkout = Workout(
            id = id,
            dateTime = Clock.System.now(),
            steps = steps,
            saved = true,
            archived = false
        )
        workouts.update {
            it.plus(id to initWorkout)
        }
    }
    override fun getWorkoutHistory(): Flow<List<WorkoutPreview>> {
        return workouts.map {
            val exerciseMap = fakeExerciseRepository.getAllExercises().associateBy {
                it.id
            }
            it.values
                .toList()
                .filterNot { it.archived }
                .map { workout ->
                    val exercises = workout.steps.mapNotNull {
                        exerciseMap[it.exerciseData.id]?.name
                    }.toSet()
                    WorkoutPreview(
                        id = workout.id,
                        dateTime = workout.dateTime,
                        exercises = exercises.toList(),
                        saved = workout.saved,
                        archived = workout.archived
                    )
                }
        }
    }

    override suspend fun getWorkoutById(id: String): Workout {
        val map = fakeExerciseRepository.getAllExercises().associateBy {
            it.id
        }
        val workout = workouts.value[id]!!
        return workout.copy(
            steps = workout.steps.map {
                val stub = it.exerciseData
                it.copy(exerciseData = map[stub.id] ?: stub)
            }
        )
    }

    override suspend fun getAll(): List<Workout> {
        return workouts.value.map { (k, v ) -> v }
    }

    override suspend fun saveWorkout(workout: Workout): String {
        return if(workout.id.isEmpty()){
            val id = generateId()
            val steps = workout.steps.map {
                it.copy(
                    id = generateId(),
                    workoutId = id,
                )
            }
            workouts.update {
                it + (id to workout.copy(
                        id = id,
                steps = steps
                ) )
            }
            id
        }else{
            workouts.update {
                it + (workout.id to workout)
            }
            workout.id
        }
    }

    override suspend fun commitWorkoutSave(id: String) {
        workouts.update { map ->
            map[id]?.let {
                map + (it.id to it.copy(saved = true))
            } ?: map
        }

    }

    override suspend fun archiveWorkout(id: String) {
        workouts.update { map ->
            map[id]?.let {
                map + (it.id to it.copy(archived = true))
            } ?: map
        }
    }

    override suspend fun deleteWorkout(id: String) {
        workouts.update {
            it - id
        }
    }

    override suspend fun getExerciseHistory(exerciseId: String): List<ExerciseWorkout> {
        return emptyList()
    }
}