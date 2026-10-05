package com.danilkha.trainstats.features.settings.workoutimport.ui

import android.net.Uri
data class ImportState(
    val exportText: String = "",
    val isLoading: Boolean = false,
    val errorLine: ImportTextRange? = null
)

data class ImportTextRange(val start: Int, val end: Int)

sealed interface ImportEvent {
    data class ChangeImportText(val text: String) : ImportEvent
    data object ImportFromText : ImportEvent
    data class ImportFromFile(val uri: Uri?) : ImportEvent
    data class ImportComplete(val success: Boolean) : ImportEvent
}

sealed interface ImportSideEffect{
    class ImportSuccess(val exercises: Int, val workouts: Int) : ImportSideEffect
    object Error : ImportSideEffect
}
