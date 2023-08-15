package com.danilkha.androidframework.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class MviViewModel<State, Event, UserEvent: Event, SideEffect> : ViewModel(){

    private val _state by lazy { MutableStateFlow(startState) }
    val state: StateFlow<State>
        get() = _state.asStateFlow()

    private val _sideEffects = MutableSharedFlow<SideEffect>()
    val sideEffects: SharedFlow<SideEffect>
        get() = _sideEffects.asSharedFlow()

    private val events = MutableSharedFlow<Event>()

    init {
        viewModelScope.launch {
            events.collect{ event ->
                processEventInternal(event)
            }
        }
    }


    abstract val startState: State

    protected open fun reduce(currentState: State, event: Event): State{
        return currentState
    }

    protected open suspend fun onSideEffect(prevState: State, newState: State, event: Event){

    }

    suspend fun showSideEffect(effect: SideEffect){
        _sideEffects.emit(effect)
    }

    fun processEvent(event: Event){
        viewModelScope.launch {
            events.emit(event)
        }
    }

    private suspend fun processEventInternal(event: Event){
        val currentState = state.value
        val newState = reduce(currentState, event)
        _state.emit(newState)
        onSideEffect(currentState, newState, event)
    }
}

@Composable
fun <State, Event, UserEvent: Event, SideEffect> MviViewModel<State, Event, UserEvent, SideEffect>.LaunchCollectEffects(collector: (FlowCollector<SideEffect>)){
    LaunchedEffect(Unit){
        sideEffects.collect(collector)
    }
}