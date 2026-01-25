package com.danilkha.trainstats.di

import com.danilkha.trainstats.features.exercises.ui.ExerciseListViewModel
import com.danilkha.trainstats.features.exercises.ui.editor.ExerciseEditorViewModel
import com.danilkha.trainstats.features.exercises.ui.history.ExerciseHistoryViewModel
import com.danilkha.trainstats.features.settings.export.ui.ExportViewModel
import com.danilkha.trainstats.features.settings.workoutimport.ui.ImportViewModel
import com.danilkha.trainstats.features.workout.ui.editor.WorkoutViewModel
import com.danilkha.trainstats.features.workout.ui.history.HistoryViewModel

interface ViewModelsProvider {

    val exerciseListViewModel: ExerciseListViewModel
    val exerciseEditorViewModel: ExerciseEditorViewModel
    val historyViewModel: HistoryViewModel
    val workoutViewModel: WorkoutViewModel
    val profileViewModel: ImportViewModel
    val exerciseHistoryViewModel : ExerciseHistoryViewModel
    val exportViewModel: ExportViewModel
}