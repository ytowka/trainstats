package com.danilkha.trainstats.features.settings.export.ui

import androidx.lifecycle.viewModelScope
import com.danilkha.commoncore.viewmodel.MviViewModel
import com.danilkha.trainstats.features.settings.export.domain.ExportWorkoutUseCase
import kotlinx.coroutines.launch

class ExportViewModel(
    private val exportWorkoutUseCase: ExportWorkoutUseCase,
) : MviViewModel<ExportState, ExportEvent, ExportSideEffect>(){

    override val startState: ExportState = ExportState.Init

    override fun reduce(
        state: ExportState,
        event: ExportEvent
    ): ExportState {
        return when(event) {
            ExportEvent.Export -> ExportState.Loading
            is ExportEvent.ExportResult -> {
                event.result.getOrNull()?.let {
                    return ExportState.Saved(it)
                }
                event.result.exceptionOrNull()?.let {
                    return ExportState.Error(it)
                }
                state
            }
        }
    }

    override suspend fun afterReduce(newState: ExportState, event: ExportEvent) {
        when(event) {
            ExportEvent.Export -> viewModelScope.launch {
                val exportResult = exportWorkoutUseCase.invoke()
                processEvent(ExportEvent.ExportResult(exportResult))
            }
            else -> Unit
        }
    }
}

sealed interface ExportState {
    data object Init : ExportState
    data object Loading : ExportState
    data class Saved(val filename: String) : ExportState
    data class Error(val throwable: Throwable) : ExportState
}

sealed interface ExportSideEffect {

}

sealed interface ExportEvent {
    object Export : ExportEvent
    data class ExportResult(val result: Result<String>) : ExportEvent
}

