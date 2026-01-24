package com.danilkha.trainstats.features.settings.workoutimport.ui

import com.danilkha.commoncore.viewmodel.MviViewModel
import com.danilkha.trainstats.features.settings.workoutimport.domain.ImportWorkoutsUseCase
import javax.inject.Inject

class ImportViewModel @Inject constructor(
    private val importWorkoutsUseCase: ImportWorkoutsUseCase
) : MviViewModel<ImportState, ImportEvent, ImportSideEffect>(){
    override val startState: ImportState = ImportState()


    override fun reduce(
        state: ImportState,
        event: ImportEvent
    ): ImportState {
        return when(event) {
            is ImportEvent.ChangeImportText -> state.copy(exportText = event.text)
            is ImportEvent.ImportFromFile -> state.copy(isLoading = true)
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
                export(ImportWorkoutsUseCase.Params.File(uri))
            }
            ImportEvent.ImportFromText -> export(ImportWorkoutsUseCase.Params.Text(newState.exportText))
            else -> {}
        }
    }

    private suspend fun export(params: ImportWorkoutsUseCase.Params){
        importWorkoutsUseCase(params).onSuccess { result ->
            when(result){
                is ImportWorkoutsUseCase.Result.Error -> {
                    showSideEffect(ImportSideEffect.Error)
                    processEvent(ImportEvent.ImportComplete(false))
                }
                is ImportWorkoutsUseCase.Result.Success ->{
                    showSideEffect(ImportSideEffect.ImportSuccess(result.exercises, result.workouts))
                    processEvent(ImportEvent.ImportComplete(true))
                }
            }
        }
    }
}