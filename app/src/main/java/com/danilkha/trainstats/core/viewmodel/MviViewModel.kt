package com.danilkha.trainstats.core.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class MviViewModel<State, Event, SideEffect> : BaseViewModel<State, Event>(){


    private val events = MutableSharedFlow<Event>()

    init {
        viewModelScope.launch {
            events.collect{ event ->
                processEventInternal(event)
            }
        }
    }

    protected open fun reduce(currentState: State, event: Event): State{
        return currentState
    }

    protected open suspend fun onSideEffect(prevState: State, newState: State, event: Event){

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