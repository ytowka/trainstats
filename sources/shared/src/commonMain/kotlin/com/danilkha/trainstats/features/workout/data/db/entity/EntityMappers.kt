package com.danilkha.trainstats.features.workout.data.db.entity

import com.danilkha.trainstats.db.entity.ExerciseSetEntity
import com.danilkha.trainstats.db.entity.ExerciseSetWithData
import com.danilkha.trainstats.db.entity.RepetitionsDb
import com.danilkha.trainstats.db.entity.WorkoutEntity
import com.danilkha.trainstats.db.entity.WorkoutWithExercises
import com.danilkha.trainstats.features.exercises.data.db.toDomain
import com.danilkha.trainstats.features.workout.domain.model.ExerciseSet
import com.danilkha.trainstats.features.workout.domain.model.Kg
import com.danilkha.trainstats.features.workout.domain.model.Repetitions
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.trainstats.features.workout.domain.model.WorkoutPreview

fun RepetitionsDb.toDomain(): Repetitions{
    return when(this){
        is RepetitionsDb.Double -> Repetitions.Double(left, right)
        is RepetitionsDb.Single -> Repetitions.Single(reps)
    }
}

fun Repetitions.toEntity(): RepetitionsDb{
    return when(this){
        is Repetitions.Double -> RepetitionsDb.Double(left, right)
        is Repetitions.Single -> RepetitionsDb.Single(reps)
    }
}

fun WorkoutEntity.toPreview() =  WorkoutPreview(
    id = id,
    dateTime = dateTime,
    exercises = exercises,
    saved = saved,
    archived = archived,
    lastEdited = lastEdited
)

fun Workout.toEntity() = WorkoutEntity(
    id = id,
    dateTime = dateTime,
    saved = saved,
    exercises = steps.map {
        it.exerciseData.name
    }.toSet().toList(),
    archived = archived,
    lastEdited = lastEdited
)

fun ExerciseSet.toEntity() =  ExerciseSetEntity(
    id = id,
    workoutId = workoutId,
    exerciseId = exerciseData.id,
    reps = reps.toEntity(),
    weightKg = weight?.value,
    orderPosition = orderPosition
)

fun ExerciseSetWithData.toDomain() = ExerciseSet(
    id = setEntity.id,
    workoutId = setEntity.workoutId,
    exerciseData = exercise.toDomain(),
    reps = setEntity.reps.toDomain(),
    weight = setEntity.weightKg?.let { Kg(it) },
    orderPosition = setEntity.orderPosition
)

fun WorkoutWithExercises.toDomain() = Workout(
    id = workout.id,
    dateTime = workout.dateTime,
    steps = exercises.map {
        it.toDomain()
    },
    saved = workout.saved,
    archived = workout.archived,
    lastEdited = workout.lastEdited
)
