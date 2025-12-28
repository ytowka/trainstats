package com.danilkha.trainstats.features.exercises.ui

import androidx.lifecycle.viewModelScope
import com.danilkha.trainstats.core.viewmodel.MviViewModel
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.exercises.domain.usecase.GetAllExercisesUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExerciseListViewModel @Inject constructor(
    private val getAllExercisesUseCase: GetAllExercisesUseCase,
): MviViewModel<ExerciseListState, ExerciseListEvent, ExerciseListSideEffect>(){
    override val startState: ExerciseListState = ExerciseListState()

    init {
        viewModelScope.launch {
            state
                .map { it.searchQuery }
                .distinctUntilChanged()
                .collectLatest {
                    updateList(it)
                }
        }
    }


    override fun reduce(
        state: ExerciseListState,
        event: ExerciseListEvent
    ): ExerciseListState {
        return when(event) {
            is ExerciseListEvent.ChangeSearchQuery -> state.copy(searchQuery = event.text)
            is ExerciseListEvent.UpdateExerciseList -> state.copy(exerciseList = event.exercises)
            is ExerciseListEvent.OnExerciseClicked -> state.copy(searchQuery = "")
            else -> state
        }
    }

    override suspend fun beforeReduce(prevState: ExerciseListState, event: ExerciseListEvent) {
        when(event) {
            ExerciseListEvent.UpdateList -> updateList(prevState.searchQuery)
            is ExerciseListEvent.OnExerciseClicked -> {
                showSideEffect(ExerciseListSideEffect.ExerciseClicked(event.exerciseModel))
            }
            else -> {}
        }
    }

    private suspend fun updateList(query: String){
        val exercises = getAllExercisesUseCase(query).getOrNull() ?: return
        processEvent(ExerciseListEvent.UpdateExerciseList(exercises.map(ExerciseData::toModel)))
    }
}