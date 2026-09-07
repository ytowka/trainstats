package com.danilkha.commoncore.viewmodel

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IMviViewModel<State, Event, SideEffect> {

    val state: StateFlow<State>
    val sideEffects: SharedFlow<SideEffect>
    fun processEvent(event: Event)
}