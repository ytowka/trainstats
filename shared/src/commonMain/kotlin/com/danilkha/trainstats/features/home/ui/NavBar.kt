package com.danilkha.trainstats.features.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import com.danilkha.commonds.components.NavigationBar
import com.danilkha.commonds.components.NavigationItem
import com.danilkha.commonds.theme.TrainingStatsTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import training_stats.common_ds.generated.resources.Res
import training_stats.common_ds.generated.resources.*
import training_stats.shared.generated.resources.Res as SharedRes
import training_stats.shared.generated.resources.*

val MainNavigationItem.icon: Painter
    @Composable
    get() = painterResource(
        when (this) {
            MainNavigationItem.HOME -> Res.drawable.ic_home
            MainNavigationItem.EXERCISES -> Res.drawable.ic_exercise
            //NavigationItem.WORKOUTS -> drawable.ic_list
            //NavigationItem.STATS -> drawable.ic_chart
            MainNavigationItem.PROFILE -> Res.drawable.ic_person
        }
    )

val MainNavigationItem.label: String
    @Composable
    get() = stringResource(when (this) {
            MainNavigationItem.HOME -> SharedRes.string.navigation_item_home
            MainNavigationItem.EXERCISES -> SharedRes.string.navigation_item_exercises
            //NavigationItem.WORKOUTS -> R.string.navigation_item_workouts
            //NavigationItem.STATS -> R.string.navigation_item_stats
            MainNavigationItem.PROFILE -> SharedRes.string.navigation_item_profile
        }
    )

enum class MainNavigationItem {
    HOME,
    EXERCISES,
    //STATS,
    PROFILE
}

@Preview
@Composable
fun NavigationBarPreview() {
    var selectedItem by remember { mutableStateOf(MainNavigationItem.HOME) }

    TrainingStatsTheme {
        NavigationBar(
            items = MainNavigationItem.entries,
            selectedItem = selectedItem, onItemClicked = { selectedItem = it },
            itemFactory = {
                NavigationItem(
                    label = it.label,
                    icon = it.icon
                )
            }
        )
    }
}
