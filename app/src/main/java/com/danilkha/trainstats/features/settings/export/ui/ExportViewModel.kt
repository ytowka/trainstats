package com.danilkha.trainstats.features.settings.export.ui

import androidx.lifecycle.viewModelScope
import com.danilkha.trainstats.core.viewmodel.BaseViewModel
import com.danilkha.trainstats.features.settings.export.domain.ExportWorkoutUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExportViewModel @Inject constructor(
    private val exportWorkoutUseCase: ExportWorkoutUseCase,
) : BaseViewModel<ExportState, ExportSideEffect>(){

    override val startState: ExportState = ExportState.Init

    fun export() {
        viewModelScope.launch {
            _state.value = ExportState.Loading
            exportWorkoutUseCase.invoke().onSuccess {
                _state.value = ExportState.Saved(it)
            }.onFailure {
                _state.value = ExportState.Error(it)
            }
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
