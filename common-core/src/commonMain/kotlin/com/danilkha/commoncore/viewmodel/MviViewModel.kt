package com.danilkha.commoncore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class MviViewModel<State, Event, SideEffect> : ViewModel() {

    protected val _state by lazy { MutableStateFlow(startState) }
    val state: StateFlow<State>
        get() = _state
            .asStateFlow()
            .onStart {
                loadData()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = startState
            )

    protected val _sideEffects = MutableSharedFlow<SideEffect>(extraBufferCapacity = 10)

    val sideEffects: SharedFlow<SideEffect>
        get() = _sideEffects.asSharedFlow()

    abstract val startState: State

    protected open suspend fun loadData() { }


    private val events = MutableSharedFlow<Event>()

    init {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            events.collect { event ->
                launch {
                    processEventInternal(event)
                }
            }
        }
    }

    fun showSideEffect(effect: SideEffect) {
        viewModelScope.launch {
            _sideEffects.emit(effect)
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

    fun <T> Flow<Result<T>>.collectResult(onFailure: (Throwable) -> Unit = {}, onSuccess: suspend (T) -> Unit) {
        viewModelScope.launch {
            collect {
                it.onSuccess {
                    onSuccess(it)
                }.onFailure(onFailure)
            }
        }
    }
}