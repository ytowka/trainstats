package com.danilkha.trainstats.di

import com.danilkha.trainstats.features.exercises.data.FakeExerciseRepository
import com.danilkha.trainstats.features.exercises.data.repository.ExerciseRepositoryImpl
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.profile.data.WorkoutParserImpl
import com.danilkha.trainstats.features.profile.domain.WorkoutParser
import com.danilkha.trainstats.features.workout.data.FakeWorkoutRepository
import com.danilkha.trainstats.features.workout.data.repository.WorkoutRepositoryImpl
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(exerciseRepository: ExerciseRepositoryImpl): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(workoutRepository: WorkoutRepositoryImpl) : WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutParser(workoutParser: WorkoutParserImpl): WorkoutParser
}