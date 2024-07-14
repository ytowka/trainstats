package com.danilkha.trainstats.features.exercises.ui

import androidx.lifecycle.viewModelScope
import com.danilkha.trainstats.core.viewmodel.BaseViewModel
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.exercises.domain.usecase.FindExercisesUseCase
import com.danilkha.trainstats.features.exercises.domain.usecase.GetAllExercisesUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExerciseListViewModel @Inject constructor(
    private val getAllExercisesUseCase: GetAllExercisesUseCase,
    private val findExercisesUseCase: FindExercisesUseCase,
): BaseViewModel<ExerciseListState, Unit>(){
    override val startState: ExerciseListState = ExerciseListState()



    init {
        viewModelScope.launch {
            state
                .map { it.searchQuery }
                .distinctUntilChanged()
                .collectLatest {
                    if(it.isNotBlank()){
                        val exercises = getAllExercisesUseCase().getOrNull() ?: return@collectLatest
                        _state.update {
                            it.copy(exerciseList = exercises.map(ExerciseData::toModel))
                        }
                    }else{
                        val exercises = findExercisesUseCase(it).getOrNull() ?: return@collectLatest
                        _state.update {
                            it.copy(exerciseList = exercises.map(ExerciseData::toModel))
                        }
                    }
                }
        }
    }

    fun queryUpdated(text: String){
        _state.update {
            it.copy(searchQuery = text)
        }
    }
}