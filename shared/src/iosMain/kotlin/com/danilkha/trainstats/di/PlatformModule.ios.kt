package com.danilkha.trainstats.di

import com.danilkha.trainstats.entrypoint.db.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory() }
}
