package com.danilkha.commonds.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PreviewContent(content: @Composable BoxScope.() -> Unit) {
    TrainingStatsTheme {
        Box(
            modifier = Modifier.background(color = MaterialTheme.colors.background),
            content = content
        )
    }
}