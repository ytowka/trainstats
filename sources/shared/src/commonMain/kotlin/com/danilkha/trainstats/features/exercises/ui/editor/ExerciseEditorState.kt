package com.danilkha.trainstats.features.exercises.ui.editor

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseVariation
import com.danilkha.trainstats.features.exercises.domain.model.validVariationNames
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.exercises.ui.ExerciseModel

data class ExerciseEditorState(
    val mode: ExerciseEditorMode = ExerciseEditorMode.New,
    val name: String = "",
    val separated: Boolean = false,
    val withWeight: Boolean = true,
    val variations: List<ExerciseVariation> = emptyList(),
) {
    val isValid = name.isNotBlank() && validVariationNames(variations)

    val isChanged by lazy {
        if (mode is ExerciseEditorMode.Edit){
            mode.initial.let {
                it.name != name ||
                        it.hasWeight != withWeight ||
                        it.separated != separated || it.variations != variations
            }
        }else true
    }
}

sealed interface ExerciseEditorMode{
    object New : ExerciseEditorMode

    class Edit(val initial: ExerciseModel) : ExerciseEditorMode
}

sealed interface ExerciseEditorSingleEvent{
    object Saved : ExerciseEditorSingleEvent
}

sealed interface ExerciseEditorEvent {
    data class Init(val editingId: String?) : ExerciseEditorEvent
    data class EditName(val name: String) : ExerciseEditorEvent
    data class SetSeparated(val separated: Boolean) : ExerciseEditorEvent
    data class SetWithWeight(val withWeight: Boolean) : ExerciseEditorEvent
    data object AddVariation : ExerciseEditorEvent
    data class RenameVariation(val id: String, val name: String) : ExerciseEditorEvent
    data class RemoveVariation(val id: String) : ExerciseEditorEvent
    object Save : ExerciseEditorEvent
    object Delete : ExerciseEditorEvent
    data class ExerciseLoaded(val data: ExerciseData) : ExerciseEditorEvent
}
