package com.danilkha.trainstats.core.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.danilkha.commoncore.viewmodel.MviViewModel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectLatest

@Composable
fun <S, E, SE> MviViewModel<S, E, SE>.collectSingleEvents(onEvent: (event: SE) -> Unit){
    LaunchedEffect(key1 = Unit) {
        sideEffects.collectLatest {
            onEvent(it)
        }
    }
}

@Composable
fun <S, E, SE> MviViewModel<S, E, SE>.LaunchCollectEffects(collector: (FlowCollector<SE>)) {
    LaunchedEffect(Unit) {
        sideEffects.collect(collector)
    }
}
