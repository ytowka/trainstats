package com.danilkha.trainstats.entrypoint

import android.app.Application
import com.danilkha.trainstats.di.AppComponent
import com.danilkha.trainstats.di.DaggerAppComponent
import com.danilkha.trainstats.features.workout.ui.WorkoutComponentDepsStore

class App : Application(){

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent
            .factory()
            .create(this)

        WorkoutComponentDepsStore.deps = appComponent
    }
}