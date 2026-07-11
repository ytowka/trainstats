package com.danilkha.trainstats.core.viewmodel

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.danilkha.commoncore.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("ComposableNaming")
@Composable
fun <S, E> BaseViewModel<S, E>.collectSingleEvents(onEvent: (event: E) -> Unit){
    LaunchedEffect(key1 = Unit) {
        sideEffects.collectLatest {
            onEvent(it)
        }
    }
}

@Composable
fun <State, SideEffect> BaseViewModel<State, SideEffect>.LaunchCollectEffects(collector: (FlowCollector<SideEffect>)) {
    LaunchedEffect(Unit) {
        sideEffects.collect(collector)
    }
}
