package com.danilkha.trainstats.features.exercises.ui.editor

import androidx.lifecycle.viewModelScope
import com.danilkha.commoncore.viewmodel.MviViewModel
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.exercises.domain.usecase.CreateExercisesUseCase
import com.danilkha.trainstats.features.exercises.domain.usecase.DeleteExercisesUseCase
import com.danilkha.trainstats.features.exercises.domain.usecase.GetExercisesUseCase
import com.danilkha.trainstats.features.exercises.domain.usecase.UpdateExercisesUseCase
import com.danilkha.trainstats.features.exercises.ui.toModel
import kotlinx.coroutines.launch

class ExerciseEditorViewModel(
    private val getExercisesUseCase: GetExercisesUseCase,
    private val createExercisesUseCase: CreateExercisesUseCase,
    private val updateExercisesUseCase: UpdateExercisesUseCase,
    private val deleteExercisesUseCase: DeleteExercisesUseCase,
) : MviViewModel<ExerciseEditorState, ExerciseEditorEvent, ExerciseEditorSingleEvent>() {
    override val startState: ExerciseEditorState = ExerciseEditorState()

    override fun reduce(
        state: ExerciseEditorState,
        event: ExerciseEditorEvent
    ): ExerciseEditorState {
        return when (event) {
            is ExerciseEditorEvent.EditName -> state.copy(name = event.name)
            is ExerciseEditorEvent.SetSeparated -> state.copy(separated = event.separated)
            is ExerciseEditorEvent.SetWithWeight -> state.copy(withWeight = event.withWeight)
            is ExerciseEditorEvent.ExerciseLoaded -> state.copy(
                mode = ExerciseEditorMode.Edit(event.data.toModel()),
                name = event.data.name,
                separated = event.data.separated,
                withWeight = event.data.hasWeight
            )
            is ExerciseEditorEvent.Init,
            ExerciseEditorEvent.Save,
            ExerciseEditorEvent.Delete -> state
        }
    }

    override suspend fun afterReduce(newState: ExerciseEditorState, event: ExerciseEditorEvent) {
        when (event) {
            is ExerciseEditorEvent.Init -> event.editingId?.let { id ->
                viewModelScope.launch {
                    getExercisesUseCase(id).onSuccess { data ->
                        processEvent(ExerciseEditorEvent.ExerciseLoaded(data))
                    }
                }
            }
            ExerciseEditorEvent.Save -> viewModelScope.launch {
                val exercise = ExerciseData(
                    id = (newState.mode as? ExerciseEditorMode.Edit)?.initial?.id ?: 0L,
                    name = newState.name,
                    separated = newState.separated,
                    hasWeight = newState.withWeight,
                    imageUrl = null
                )
                when (newState.mode) {
                    is ExerciseEditorMode.Edit -> {
                        if (newState.isValid) {
                            updateExercisesUseCase(exercise)
                            showSideEffect(ExerciseEditorSingleEvent.Saved)
                        }
                    }
                    ExerciseEditorMode.New -> {
                        if (newState.isValid) {
                            createExercisesUseCase(exercise)
                            showSideEffect(ExerciseEditorSingleEvent.Saved)
                        }
                    }
                }
            }
            ExerciseEditorEvent.Delete -> viewModelScope.launch {
                val id = (newState.mode as? ExerciseEditorMode.Edit)?.initial?.id
                id?.let {
                    deleteExercisesUseCase(id)
                    showSideEffect(ExerciseEditorSingleEvent.Saved)
                }
            }
            is ExerciseEditorEvent.EditName,
            is ExerciseEditorEvent.SetSeparated,
            is ExerciseEditorEvent.SetWithWeight,
            is ExerciseEditorEvent.ExerciseLoaded -> Unit
        }
    }
}
