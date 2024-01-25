package com.danilkha.workout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
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

