package com.danilkha.trainstats.features.workout.ui

import kotlin.properties.Delegates.notNull

class WorkoutViewModel : com.danilkha.trainstats.core.viewmodel.BaseViewModel<WorkoutState, WorkoutSideEffect>(){

    override val startState: WorkoutState = WorkoutState(listOf(Item.Initializing()))

    val workoutComponent = DaggerWorkoutComponent.factory()
        .create(WorkoutComponentDepsProvider.deps)

}

interface WorkoutComponentDepsProvider{
    val deps: WorkoutDependencies

    companion object : WorkoutComponentDepsProvider by WorkoutComponentDepsStore
}

object WorkoutComponentDepsStore : WorkoutComponentDepsProvider {
    override var deps: WorkoutDependencies by notNull()
}

