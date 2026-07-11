package com.danilkha.trainstats.di

import com.danilkha.trainstats.features.settings.export.data.FileWriter
import com.danilkha.trainstats.features.settings.export.domain.ExportWorkoutUseCase
import com.danilkha.trainstats.features.settings.workoutimport.data.FileReader
import com.danilkha.trainstats.features.settings.workoutimport.domain.ImportWorkoutsUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val androidSharedModule: Module = module {
    singleOf(::FileWriter)
    singleOf(::FileReader)
    factoryOf(::ExportWorkoutUseCase)
    factoryOf(::ImportWorkoutsUseCase)
}
