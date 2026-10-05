package com.danilkha.trainstats.features.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import com.danilkha.navigation.api.destinations.RootNav
import com.danilkha.navigation.api.destinations.SettingsNav
import com.danilkha.navigation.api.destinations.WorkoutNav
import com.danilkha.navigation.impl.NavHost
import com.danilkha.trainstats.features.exercises.ui.ExerciseListScreenPage
import com.danilkha.trainstats.features.home.ui.HomeScreen
import com.danilkha.trainstats.features.home.ui.MainNavigationItem
import com.danilkha.trainstats.features.profile.ui.ProfileScreen
import com.danilkha.trainstats.features.settings.SettingsScreen
import com.danilkha.trainstats.features.settings.importExportEntries
import com.danilkha.trainstats.features.workout.ui.editor.WorkoutScreenRoute
import com.danilkha.trainstats.features.workout.ui.history.HistoryScreenPage

@Composable
fun RootScreen() {
    NavHost(
        startDestination = RootNav,
        modifier = Modifier
            .background(color = MaterialTheme.colors.background)
            .safeDrawingPadding(),
        entryProvider = entryProvider {
            entry<RootNav> {
                HomeRoot()
            }
            entry<SettingsNav> {
                SettingsScreen()
            }
            entry<WorkoutNav> { key ->
                WorkoutScreenRoute(workoutId = key.id)
            }
            importExportEntries()
        }
    )
}

/** Хост нижних табов: HOME / EXERCISES / PROFILE. Табы — внутреннее состояние, не навигация. */
@Composable
private fun HomeRoot() {
    var currentPageItem by rememberSaveable { mutableStateOf(MainNavigationItem.HOME) }

    HomeScreen(
        currentPageItem = currentPageItem,
        onChange = { currentPageItem = it }
    ) {
        when (it) {
            MainNavigationItem.HOME -> HistoryScreenPage()
            MainNavigationItem.EXERCISES -> ExerciseListScreenPage()
            MainNavigationItem.PROFILE -> ProfileScreen()
        }
    }
}
