package com.danilkha.trainstats.core.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class MviViewModel<State, Event, SideEffect> : BaseViewModel<State, SideEffect>() {

    private val events = MutableSharedFlow<Event>()

    init {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            events.collect(::processEventInternal)
        }
    }

    abstract fun reduce(state: State, event: Event): State

    protected open suspend fun beforeReduce(prevState: State, event: Event) {

    }

    protected open suspend fun afterReduce(newState: State, event: Event) {

    }

    fun processEvent(event: Event) {
        viewModelScope.launch {
            events.emit(event)
        }
    }

    private suspend fun processEventInternal(event: Event) {
        val newState =_state.updateAndGet { currentState ->
            beforeReduce(prevState = currentState, event)
            reduce(currentState, event)
        }
        afterReduce(newState, event)
    }
}