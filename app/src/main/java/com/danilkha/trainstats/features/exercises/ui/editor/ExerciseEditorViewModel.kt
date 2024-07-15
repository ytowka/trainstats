package com.danilkha.trainstats.features.exercises.ui.editor

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.danilkha.trainstats.core.viewmodel.BaseViewModel
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.exercises.domain.usecase.CreateExercisesUseCase
import com.danilkha.trainstats.features.exercises.domain.usecase.DeleteExercisesUseCase
import com.danilkha.trainstats.features.exercises.domain.usecase.GetExercisesUseCase
import com.danilkha.trainstats.features.exercises.domain.usecase.UpdateExercisesUseCase
import com.danilkha.trainstats.features.exercises.ui.toModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class ExerciseEditorViewModel @Inject constructor(
    private val getExercisesUseCase: GetExercisesUseCase,
    private val createExercisesUseCase: CreateExercisesUseCase,
    private val updateExercisesUseCase: UpdateExercisesUseCase,
    private val deleteExercisesUseCase: DeleteExercisesUseCase,
) : BaseViewModel<ExerciseEditorState, ExerciseEditorSingleEvent>(){
    override val startState: ExerciseEditorState = ExerciseEditorState()


    fun init(editingId: Long?){
        if(editingId != null){
            viewModelScope.launch {
                getExercisesUseCase(editingId).onSuccess { data ->
                    _state.update {
                        it.copy(
                            mode = ExerciseEditorMode.Edit(data.toModel()),
                            name = data.name,
                            separated = data.separated,
                            withWeight = data.hasWeight
                        )
                    }
                }
            }
        }
    }

    fun editName(name: String){
        _state.update {
            it.copy(name = name)
        }
    }

    fun setSeparated(separated: Boolean){
        _state.update {
            it.copy(
                separated = separated
            )
        }
    }

    fun setWithWeight(withWeight: Boolean){
        _state.update {
            it.copy(withWeight = withWeight)
        }
    }

    fun delete(){
        viewModelScope.launch {
            val id = (_state.value.mode as? ExerciseEditorMode.Edit)?.initial?.id
            id?.let {
                deleteExercisesUseCase(id)
                showSideEffect(ExerciseEditorSingleEvent.Saved)
            }
        }
    }
    fun save(){
        viewModelScope.launch {
                val state = _state.value
                val exercise = ExerciseData(
                    id = (state.mode as? ExerciseEditorMode.Edit)?.initial?.id ?: 0L,
                    name = state.name,
                    separated = state.separated,
                    hasWeight = state.withWeight,
                    imageUrl = null
                )
                when(state.mode){
                    is ExerciseEditorMode.Edit -> {
                        if(state.isValid){
                            updateExercisesUseCase(exercise)
                            showSideEffect(ExerciseEditorSingleEvent.Saved)
                        }
                    }
                    ExerciseEditorMode.New -> {
                        if(state.isValid){
                            createExercisesUseCase(exercise)
                            showSideEffect(ExerciseEditorSingleEvent.Saved)
                        }
                    }
                }

        }
    }
}