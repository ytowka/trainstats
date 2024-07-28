package com.danilkha.trainstats.features.profile.ui

import androidx.compose.ui.text.TextRange
import androidx.lifecycle.viewModelScope
import com.danilkha.trainstats.core.viewmodel.BaseViewModel
import com.danilkha.trainstats.features.profile.domain.ImportWorkoutsUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val importWorkoutsUseCase: ImportWorkoutsUseCase
) : BaseViewModel<ProfileState, ProfileSingleEvent>(){
    override val startState: ProfileState = ProfileState()

    fun onExportTextChange(text: String){
        update {
            it.copy(exportText = text)
        }
    }


    fun onExportClicked(){
        update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            importWorkoutsUseCase(ImportWorkoutsUseCase.Params(_state.value.exportText)).onSuccess { result ->
                when(result){
                    is ImportWorkoutsUseCase.Result.Error -> {
                        showSideEffect(ProfileSingleEvent.Error)
                        update { state ->
                           /* var startIndex = 0
                            var endIndex = 0

                            var currentLine = 0
                            while (currentLine <= result.invalidLine){
                                startIndex = endIndex
                                endIndex = state.exportText.indexOf('\n', startIndex + 1)
                                currentLine++
                            }*/
                            state.copy(
                                //errorLine = TextRange(startIndex, endIndex),
                                isLoading = false
                            )
                        }
                    }
                    is ImportWorkoutsUseCase.Result.Success ->{
                        showSideEffect(ProfileSingleEvent.ImportSuccess(result.exercises, result.workouts))
                        update {
                            it.copy(
                                exportText = "",
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }
}