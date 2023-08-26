package com.danilkha.workout

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import javax.inject.Scope
import javax.inject.Singleton


@Scope
annotation class WorkoutScope

@WorkoutScope
@Component(
    dependencies = [WorkoutDependencies::class]
)
abstract class WorkoutComponent {

    @Component.Factory
    interface Factory{
        fun create(
            dependencies: WorkoutDependencies
        ): WorkoutComponent
    }
}

interface WorkoutDependencies{

}