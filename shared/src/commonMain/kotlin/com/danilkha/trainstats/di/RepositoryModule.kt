package com.danilkha.trainstats.di

import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.data.repository.ExerciseRepositoryImpl
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.data.repository.WorkoutRepositoryImpl
import com.danilkha.trainstats.features.settings.workoutimport.domain.WorkoutParser
import com.danilkha.trainstats.features.settings.workoutimport.data.WorkoutParserImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule: Module = module {
    singleOf(::WorkoutRepositoryImpl) bind WorkoutRepository::class
    singleOf(::ExerciseRepositoryImpl) bind ExerciseRepository::class
    singleOf(::WorkoutParserImpl) bind WorkoutParser::class
}
