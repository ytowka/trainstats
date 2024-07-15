package com.danilkha.trainstats.di

import com.danilkha.trainstats.features.exercises.data.FakeExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(fakeExerciseRepository: FakeExerciseRepository): ExerciseRepository
}