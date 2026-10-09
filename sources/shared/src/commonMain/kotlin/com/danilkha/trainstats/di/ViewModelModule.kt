package com.danilkha.trainstats.di

import com.danilkha.trainstats.features.exercises.ui.ExerciseListViewModel
import com.danilkha.trainstats.features.exercises.ui.editor.ExerciseEditorViewModel
import com.danilkha.trainstats.features.workout.ui.exercisehistory.ExerciseHistoryViewModel
import com.danilkha.trainstats.features.workout.ui.editor.WorkoutSaver
import com.danilkha.trainstats.features.workout.ui.editor.WorkoutViewModel
import com.danilkha.trainstats.features.workout.ui.history.HistoryViewModel
import com.danilkha.trainstats.features.stats.ui.StatisticsViewModel
import com.danilkha.trainstats.features.workout.ui.details.WorkoutDetailsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val viewModelModule: Module = module {
    factoryOf(::StatisticsViewModel)
    factoryOf(::WorkoutDetailsViewModel)
    singleOf(::WorkoutSaver)
    factoryOf(::WorkoutViewModel)
    factoryOf(::ExerciseListViewModel)
    factoryOf(::HistoryViewModel)
    factoryOf(::ExerciseHistoryViewModel)
    factoryOf(::ExerciseEditorViewModel)
}
