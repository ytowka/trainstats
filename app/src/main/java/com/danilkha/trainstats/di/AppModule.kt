package com.danilkha.trainstats.di

import com.danilkha.trainstats.features.exercises.ui.ExerciseListViewModel
import com.danilkha.trainstats.features.exercises.ui.editor.ExerciseEditorViewModel
import com.danilkha.trainstats.features.exercises.ui.history.ExerciseHistoryViewModel
import com.danilkha.trainstats.features.settings.export.ui.ExportViewModel
import com.danilkha.trainstats.features.settings.workoutimport.ui.ImportViewModel
import com.danilkha.trainstats.features.workout.ui.editor.WorkoutSaver
import com.danilkha.trainstats.features.workout.ui.editor.WorkoutViewModel
import com.danilkha.trainstats.features.workout.ui.history.HistoryViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule: Module = module {
    singleOf(::WorkoutSaver)
    viewModelOf(::WorkoutViewModel)
    viewModelOf(::ExerciseListViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::ExerciseHistoryViewModel)
    viewModelOf(::ExerciseEditorViewModel)
    viewModelOf(::ExportViewModel)
    viewModelOf(::ImportViewModel)
}
