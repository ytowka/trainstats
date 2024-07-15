package com.danilkha.trainstats.features.exercises.ui.editor

import com.danilkha.trainstats.features.exercises.ui.ExerciseModel

data class ExerciseEditorState(
    val mode: ExerciseEditorMode = ExerciseEditorMode.New,
    val name: String = "",
    val separated: Boolean = false,
    val withWeight: Boolean = true,
) {
    val isValid = name.isNotBlank()

    val isChanged by lazy {
        if (mode is ExerciseEditorMode.Edit){
            mode.initial.let {
                it.name != name ||
                        it.hasWeight != withWeight ||
                        it.separated != separated
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
