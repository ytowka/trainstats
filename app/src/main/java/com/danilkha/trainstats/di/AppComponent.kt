package com.danilkha.trainstats.di

import android.content.Context
import com.danilkha.trainstats.features.exercises.ui.ExerciseListViewModel
import com.danilkha.trainstats.features.exercises.ui.editor.ExerciseEditorViewModel
import com.danilkha.trainstats.features.workout.ui.editor.WorkoutViewModel
import com.danilkha.trainstats.features.workout.ui.history.HistoryViewModel
import dagger.BindsInstance
import dagger.Component
import javax.inject.Qualifier
import javax.inject.Singleton

@Component(modules = [RepositoryModule::class])
@Singleton
abstract class AppComponent  {


    @Component.Factory
    interface Factory{
        fun create(
            @BindsInstance
            @ApplicationContext
                   context: Context): AppComponent
    }


    abstract val exerciseListViewModel: ExerciseListViewModel
    abstract val exerciseEditorViewModel: ExerciseEditorViewModel
    abstract val historyViewModel: HistoryViewModel
    abstract val workoutViewModel: WorkoutViewModel
}

@Qualifier
annotation class ApplicationContext