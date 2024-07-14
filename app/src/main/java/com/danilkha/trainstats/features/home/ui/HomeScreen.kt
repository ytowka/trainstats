package com.danilkha.trainstats.features.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(
    currentPage: @Composable AnimatedContentScope.(NavigationItem) -> Unit
){
    var currentPageItem by remember { mutableStateOf(NavigationItem.HOME) }

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            targetState = currentPageItem,
            label = "",
            content = currentPage,
        )
        NavigationBar(
            selectedItem = currentPageItem,
            onItemClicked = {currentPageItem = it}
        )
    }
}

