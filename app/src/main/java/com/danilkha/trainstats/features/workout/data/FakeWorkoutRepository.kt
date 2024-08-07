package com.danilkha.trainstats.features.workout.data

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
import korlibs.time.DateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeWorkoutRepository @Inject constructor(
    private val fakeExerciseRepository: ExerciseRepository,
): WorkoutRepository {

    private var idSequence: Long = 0
        get() {
            field++
            return field
        }

    private val workouts = MutableStateFlow(mapOf<Long, Workout>())

    init {
        val id = idSequence
        val exerciseData1 = runBlocking{ fakeExerciseRepository.getAllExercises().firstOrNull() }
        val steps = exerciseData1?.let {
            buildList {
                add(ExerciseSet(
                    id = idSequence,
                    workoutId = id,
                    exerciseData = exerciseData1,
                    reps = Repetitions.Single(10f),
                    weight = Kg(50f),
                    orderPosition = 0,
                )
                )
                add(
                    ExerciseSet(
                        id = idSequence,
                        workoutId = id,
                        exerciseData = exerciseData1,
                        reps = Repetitions.Single(10f),
                        weight = Kg(50f),
                        orderPosition = 0
                    )
                )
                add(
                    ExerciseSet(
                        id = idSequence,
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
            dateTime = DateTime.now(),
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

    override suspend fun getWorkoutById(id: Long): Workout {
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

    override suspend fun saveWorkout(workout: Workout): Long {
        return if(workout.id == 0L){
            val id = idSequence
            val steps = workout.steps.map {
                it.copy(
                    id = idSequence,
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

    override suspend fun commitWorkoutSave(id: Long) {
        workouts.update { map ->
            map[id]?.let {
                map + (it.id to it.copy(saved = true))
            } ?: map
        }

    }

    override suspend fun archiveWorkout(id: Long) {
        workouts.update { map ->
            map[id]?.let {
                map + (it.id to it.copy(archived = true))
            } ?: map
        }
    }

    override suspend fun deleteWorkout(id: Long) {
        workouts.update {
            it - id
        }
    }

    override suspend fun getExerciseHistory(exerciseId: Long): List<ExerciseWorkout> {
        return emptyList()
    }
}