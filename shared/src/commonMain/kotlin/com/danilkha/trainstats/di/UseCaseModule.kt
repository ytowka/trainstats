package com.danilkha.trainstats.di

import com.danilkha.trainstats.features.workout.domain.usecase.*
import com.danilkha.trainstats.features.exercises.domain.usecase.*
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val useCaseModule: Module = module {
    factoryOf(::SaveWorkoutUseCase)
    factoryOf(::CommitWorkoutSaveUseCase)
    factoryOf(::GetWorkoutHistoryUseCase)
    factoryOf(::GetExerciseHistoryUseCase)
    factoryOf(::ArchiveWorkoutUseCase)
    factoryOf(::GetWorkoutByIdUseCase)
    factoryOf(::DeleteWorkoutUseCase)
    factoryOf(::GetAllExercisesUseCase)
    factoryOf(::DeleteExercisesUseCase)
    factoryOf(::CreateExercisesUseCase)
    factoryOf(::GetExercisesUseCase)
    factoryOf(::UpdateExercisesUseCase)
}
