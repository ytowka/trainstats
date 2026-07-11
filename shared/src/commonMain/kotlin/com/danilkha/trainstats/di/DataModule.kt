package com.danilkha.trainstats.di

import com.danilkha.trainstats.entrypoint.db.DatabaseDriverFactory
import com.danilkha.trainstats.entrypoint.db.TrainStatsDb
import com.danilkha.trainstats.features.exercises.data.db.RoomExerciseDatasource
import com.danilkha.trainstats.features.exercises.data.repository.ExerciseLocalDatasource
import com.danilkha.trainstats.features.workout.data.db.RoomWorkoutDatasource
import com.danilkha.trainstats.features.workout.data.repository.WorkoutLocalDatasource
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule: Module = module {
    single { get<DatabaseDriverFactory>().createBuilder().build() }
    single { get<TrainStatsDb>().exerciseDao() }
    single { get<TrainStatsDb>().workoutDao() }
    singleOf(::RoomWorkoutDatasource) bind WorkoutLocalDatasource::class
    singleOf(::RoomExerciseDatasource) bind ExerciseLocalDatasource::class
}
