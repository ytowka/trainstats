package com.danilkha.trainstats.di

import android.content.Context
import com.danilkha.trainstats.features.exercises.ui.ExerciseListViewModel
import com.danilkha.trainstats.features.workout.ui.WorkoutDependencies
import dagger.BindsInstance
import dagger.Component
import javax.inject.Qualifier
import javax.inject.Singleton

@Component(modules = [RepositoryModule::class])
@Singleton
abstract class AppComponent : WorkoutDependencies {


    @Component.Factory
    interface Factory{
        fun create(
            @BindsInstance
            @ApplicationContext
                   context: Context): AppComponent
    }

    abstract val exerciseListViewModel: ExerciseListViewModel
}

@Qualifier
annotation class ApplicationContext