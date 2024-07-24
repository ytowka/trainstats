package com.danilkha.trainstats.features.workout.data

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.ExerciseSet
import com.danilkha.trainstats.features.workout.domain.model.Kg
import com.danilkha.trainstats.features.workout.domain.model.Repetitions
import com.danilkha.trainstats.features.workout.domain.model.Workout
import korlibs.time.DateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class FakeWorkoutRepository @Inject constructor(): WorkoutRepository {

    private var idSequence: Long = 0
        get() {
            field++
            return field
        }

    private val workouts = MutableStateFlow(mapOf<Long, Workout>())

    init {
        val id = idSequence
        val exerciseData1 = ExerciseData(
            id = 1,
            name = "Жим лежа",
            imageUrl = null,
            separated = false,
            hasWeight = true
        )
        val steps = buildList {
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

    override fun getWorkoutHistory(): Flow<List<Workout>> {
        return workouts.map {
            it.values
                .toList()
                .filterNot { it.archived }
        }
    }

    override suspend fun getWorkoutById(id: Long): Workout {
        return workouts.value[id]!!
    }

    override suspend fun saveWorkout(workout: Workout) {
        if(workout.id == 0L){
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
        }else{
            workouts.update {
                it + (workout.id to workout)
            }
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
}