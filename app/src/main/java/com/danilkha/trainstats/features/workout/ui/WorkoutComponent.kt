package com.danilkha.trainstats.features.workout.ui

import dagger.Component
import javax.inject.Scope


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