package com.danilkha.trainstats.features.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.danilkha.commonds.components.NavigationBar
import com.danilkha.commonds.components.NavigationItem

@Composable
fun HomeScreen(
    currentPageItem: MainNavigationItem,
    onChange: (MainNavigationItem) -> Unit,
    currentPage: @Composable AnimatedContentScope.(MainNavigationItem) -> Unit
){

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
            items = MainNavigationItem.entries,
            selectedItem = currentPageItem,
            onItemClicked = onChange,
            itemFactory = {
                NavigationItem(
                    label = it.label,
                    icon = it.icon
                )
            }
        )
    }
}

