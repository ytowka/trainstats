package com.danilkha.workout

import androidx.lifecycle.ViewModel
import kotlin.properties.Delegates
import kotlin.properties.Delegates.notNull

class WorkoutViewModel : ViewModel(){

    val workoutComponent = DaggerWorkoutComponent
        .factory()
        .create(WorkoutComponentDepsProvider.deps)
}

interface WorkoutComponentDepsProvider{
    val deps: WorkoutDependencies

    companion object : WorkoutComponentDepsProvider by WorkoutComponentDepsStore
}

object WorkoutComponentDepsStore : WorkoutComponentDepsProvider{
    override var deps: WorkoutDependencies by notNull()
}

