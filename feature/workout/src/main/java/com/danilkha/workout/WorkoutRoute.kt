package com.danilkha.workout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

@Composable
fun WorkoutRoute(
    viewModel: WorkoutViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
){

}

internal val LocalWorkoutComponent = staticCompositionLocalOf<WorkoutComponent> { throw Exception("component not provided") }

fun WorkoutScreen(
    state: WorkoutState,
){

}

interface WorkoutDependenciesProvider{

}

