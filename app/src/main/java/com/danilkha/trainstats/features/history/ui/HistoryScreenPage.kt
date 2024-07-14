package com.danilkha.trainstats.features.history.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danilkha.uikit.components.Fab

@Composable
fun HistoryScreenPage(
    onAddClicked: () -> Unit
){
    HistoryPage(
        onAddClicked = onAddClicked
    )
}

@Composable
fun HistoryPage(
    onAddClicked: () -> Unit,
){
    Box(
        modifier = Modifier.fillMaxSize(),
    ){

        Fab(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.BottomEnd),
            onClick = onAddClicked
        )
    }
}