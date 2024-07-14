package com.danilkha.trainstats.features.workout.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WorkoutRoute(
    viewModel: WorkoutViewModel = viewModel()
){
    val state by viewModel.state.collectAsState(viewModel.startState)

    WorkoutScreen(state)
}

fun WorkoutScreen(
    state: WorkoutState,
) {

}

