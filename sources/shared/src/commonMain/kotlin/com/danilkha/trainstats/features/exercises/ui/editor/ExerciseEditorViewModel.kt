package com.danilkha.trainstats.features.exercises.ui.editor

import androidx.lifecycle.viewModelScope
import com.danilkha.commoncore.utils.generateId
import com.danilkha.commoncore.viewmodel.MviViewModel
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseVariation
import com.danilkha.trainstats.features.exercises.domain.usecase.*
import com.danilkha.trainstats.features.exercises.ui.toModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ExerciseEditorViewModel(
    private val getExercisesUseCase: GetExercisesUseCase,
    private val createExercisesUseCase: CreateExercisesUseCase,
    private val updateExercisesUseCase: UpdateExercisesUseCase,
    private val deleteExercisesUseCase: DeleteExercisesUseCase,
) : MviViewModel<ExerciseEditorState, ExerciseEditorEvent, ExerciseEditorSingleEvent>() {
    override val startState = ExerciseEditorState()
    private var loadJob: Job? = null

    override fun reduce(state: ExerciseEditorState, event: ExerciseEditorEvent): ExerciseEditorState = when (event) {
        is ExerciseEditorEvent.Init -> startState
        is ExerciseEditorEvent.EditName -> state.copy(name = event.name)
        is ExerciseEditorEvent.SetSeparated -> state.copy(separated = event.separated)
        is ExerciseEditorEvent.SetWithWeight -> state.copy(withWeight = event.withWeight)
        ExerciseEditorEvent.AddVariation -> state.copy(variations = state.variations + ExerciseVariation(
            generateId(), (state.mode as? ExerciseEditorMode.Edit)?.initial?.id.orEmpty(), "",
        ))
        is ExerciseEditorEvent.RenameVariation -> state.copy(variations = state.variations.map {
            if (it.id == event.id) it.copy(name = event.name) else it
        })
        is ExerciseEditorEvent.RemoveVariation -> state.copy(variations = state.variations.filterNot { it.id == event.id })
        is ExerciseEditorEvent.ExerciseLoaded -> state.copy(
            mode = ExerciseEditorMode.Edit(event.data.toModel()), name = event.data.name,
            separated = event.data.separated, withWeight = event.data.hasWeight, variations = event.data.variations,
        )
        ExerciseEditorEvent.Save -> state
        ExerciseEditorEvent.Delete -> state
    }

    override suspend fun afterReduce(newState: ExerciseEditorState, event: ExerciseEditorEvent) {
        when (event) {
            is ExerciseEditorEvent.Init -> {
                loadJob?.cancel()
                event.editingId?.let { id ->
                    loadJob = viewModelScope.launch {
                        getExercisesUseCase(id).onSuccess { processEvent(ExerciseEditorEvent.ExerciseLoaded(it)) }
                    }
                }
            }
            ExerciseEditorEvent.Save -> viewModelScope.launch {
                val exercise = ExerciseData(
                    id = (newState.mode as? ExerciseEditorMode.Edit)?.initial?.id ?: "",
                    name = newState.name,
                    separated = newState.separated,
                    hasWeight = newState.withWeight,
                    imageUrl = null,
                    variations = newState.variations,
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
            else -> Unit
        }
    }
}
