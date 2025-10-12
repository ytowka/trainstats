package com.danilkha.trainstats.core.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class BaseViewModel<State, SideEffect> : ViewModel() {

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

    fun showSideEffect(effect: SideEffect) {
        viewModelScope.launch {
            _sideEffects.emit(effect)
        }
    }

    protected fun update(newState: (State) -> State): State = _state.updateAndGet(newState)

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

@Composable
fun <State, SideEffect> BaseViewModel<State, SideEffect>.LaunchCollectEffects(collector: (FlowCollector<SideEffect>)) {
    LaunchedEffect(Unit) {
        sideEffects.collect(collector)
    }
}
