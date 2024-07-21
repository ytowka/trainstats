package com.danilkha.trainstats.di

import android.content.Context
import com.danilkha.trainstats.features.exercises.ui.ExerciseListViewModel
import com.danilkha.trainstats.features.exercises.ui.editor.ExerciseEditorViewModel
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
}

@Qualifier
annotation class ApplicationContext