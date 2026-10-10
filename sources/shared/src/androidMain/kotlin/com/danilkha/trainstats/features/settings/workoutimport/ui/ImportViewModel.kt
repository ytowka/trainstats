package com.danilkha.trainstats.features.settings.workoutimport.ui

import com.danilkha.commoncore.viewmodel.MviViewModel
import com.danilkha.trainstats.features.settings.workoutimport.domain.ImportWorkoutsUseCase
import kotlinx.coroutines.CancellationException

class ImportViewModel(
    private val importWorkoutsUseCase: ImportWorkoutsUseCase
) : MviViewModel<ImportState, ImportEvent, ImportSideEffect>(){
    override val startState: ImportState = ImportState()


    override fun reduce(
        state: ImportState,
        event: ImportEvent
    ): ImportState {
        return when(event) {
            is ImportEvent.ChangeImportText -> state.copy(exportText = event.text)
            is ImportEvent.ImportFromFile -> if (event.uri != null ){
                state.copy(isLoading = true)
            } else state
            ImportEvent.ImportFromText -> state.copy(isLoading = true)
            is ImportEvent.ImportComplete -> state.copy(
                isLoading = false,
                exportText = if(event.success) "" else state.exportText
            )
        }
    }

    override suspend fun afterReduce(
        newState: ImportState,
        event: ImportEvent
    ) {
        when(event) {
            is ImportEvent.ImportFromFile -> event.uri?.let { uri ->
                import(ImportWorkoutsUseCase.Params.File(uri))
            }
            ImportEvent.ImportFromText -> import(ImportWorkoutsUseCase.Params.Text(newState.exportText))
            else -> {}
        }
    }

    private suspend fun import(params: ImportWorkoutsUseCase.Params){
        importWorkoutsUseCase(params).onSuccess { result ->
            showSideEffect(ImportSideEffect.ImportSuccess(result.exercises, result.workouts))
            processEvent(ImportEvent.ImportComplete(true))
        }.onFailure { t ->
            if (t is CancellationException) throw t
            showSideEffect(ImportSideEffect.Error(t))
            processEvent(ImportEvent.ImportComplete(false))
        }
    }
}
