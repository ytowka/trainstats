package com.danilkha.trainstats.entrypoint

import android.app.Application
import com.danilkha.trainstats.di.AppComponent
import com.danilkha.trainstats.di.DaggerAppComponent
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

class App : Application(){

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        Napier.base(DebugAntilog())

        appComponent = DaggerAppComponent
            .factory()
            .create(this)
    }
}