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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.danilkha.trainstats.features.exercises.ui.ExerciseListScreenPage
import com.danilkha.trainstats.features.home.ui.HomeScreen
import com.danilkha.trainstats.features.home.ui.MainNavigationItem
import com.danilkha.trainstats.features.profile.ui.ProfileScreen
import com.danilkha.trainstats.features.settings.SettingsHostScreen
import com.danilkha.trainstats.features.workout.ui.editor.WorkoutScreenRoute
import com.danilkha.trainstats.features.workout.ui.history.HistoryScreenPage

@Composable
fun RootScreen() {

    val navController = rememberNavController()
    var currentPageItem by rememberSaveable { mutableStateOf(MainNavigationItem.HOME) }

    NavHost(
        modifier = Modifier
            .background(color = MaterialTheme.colors.background)
            .safeDrawingPadding()
        ,
        navController = navController,
        startDestination = Navigation.root
    ){
        composable(Navigation.root){
            HomeScreen(
                currentPageItem = currentPageItem,
                onChange = { currentPageItem = it }
            ) {
                when(it){
                    MainNavigationItem.HOME -> HistoryScreenPage(
                        onWorkoutClicked = {
                            navController.navigate(Navigation.Workout(it))
                        },
                        onAddClicked = {
                            navController.navigate(Navigation.Workout(null))
                        }
                    )
                    MainNavigationItem.EXERCISES -> ExerciseListScreenPage()
                    MainNavigationItem.PROFILE -> ProfileScreen(
                        onSettingsClicked = { navController.navigate(Navigation.settings) }
                    )
                }
            }
        }
        composable(Navigation.settings) {
            SettingsHostScreen(
                onBack = { navController.navigateUp() }
            )
        }
        composable(
            route = Navigation.Workout.route,
            arguments = listOf(navArgument(Navigation.Workout.idArg) {
                type = NavType.StringType
                defaultValue = ""
            })
        ){ backStackEntry ->
            val id = backStackEntry.arguments?.getString(Navigation.Workout.idArg)?.takeIf { it.isNotEmpty() }
            WorkoutScreenRoute(
                workoutId = id,
                onSaved = { navController.navigateUp() }
            )
        }
    }
}
