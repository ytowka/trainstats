package com.danilkha.trainstats.entrypoint

import android.app.Application
import com.danilkha.trainstats.di.androidSharedModule
import com.danilkha.trainstats.di.appModule
import com.danilkha.trainstats.di.dataModule
import com.danilkha.trainstats.di.platformModule
import com.danilkha.trainstats.di.repositoryModule
import com.danilkha.trainstats.di.useCaseModule
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Napier.base(DebugAntilog())
        startKoin {
            androidContext(this@App)
            modules(
                platformModule,
                dataModule,
                repositoryModule,
                useCaseModule,
                androidSharedModule,
                appModule,
            )
        }
    }
}
