package com.danilkha.trainstats.di

import com.danilkha.trainstats.features.exercises.ui.ExerciseListViewModel
import com.danilkha.trainstats.features.exercises.ui.editor.ExerciseEditorViewModel
import com.danilkha.trainstats.features.workout.ui.exercisehistory.ExerciseHistoryViewModel
import com.danilkha.trainstats.features.workout.ui.editor.WorkoutSaver
import com.danilkha.trainstats.features.workout.ui.editor.WorkoutViewModel
import com.danilkha.trainstats.features.workout.ui.history.HistoryViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule: Module = module {
    singleOf(::WorkoutSaver)
    viewModelOf(::WorkoutViewModel)
    viewModelOf(::ExerciseListViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::ExerciseHistoryViewModel)
    viewModelOf(::ExerciseEditorViewModel)
}
