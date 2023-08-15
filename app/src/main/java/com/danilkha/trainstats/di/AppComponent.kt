package com.danilkha.trainstats.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.Component.Factory
import javax.inject.Qualifier
import javax.inject.Singleton

@Component
@Singleton
abstract class AppComponent {


    @Component.Factory
    interface Factory{
        fun create(
            @BindsInstance
            @ApplicationContext
                   context: Context): AppComponent
    }
}

@Qualifier
annotation class ApplicationContext