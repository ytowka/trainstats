package com.danilkha.trainstats.di

import com.danilkha.trainstats.db.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory() }
}
