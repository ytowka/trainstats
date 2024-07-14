package com.danilkha.trainstats.features.exercises.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.danilkha.uikit.bottomsheet.ComposeContextBottomDialog

class ExerciseEditorBottomSheet : ComposeContextBottomDialog(){

    override val content: @Composable () -> Unit = {
        Spacer(modifier = Modifier.size(100.dp).background(color = Color.Cyan))
    }
}