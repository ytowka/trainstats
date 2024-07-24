package com.danilkha.trainstats.features.navigation

import androidx.compose.foundation.background
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.danilkha.trainstats.features.exercises.ui.ExerciseListScreenPage
import com.danilkha.trainstats.features.workout.ui.history.HistoryScreenPage
import com.danilkha.trainstats.features.home.ui.HomeScreen
import com.danilkha.trainstats.features.home.ui.NavigationItem
import com.danilkha.trainstats.features.workout.ui.editor.WorkoutScreenRoute

@Composable
fun RootScreen() {

    val navController = rememberNavController()
    NavHost(
        modifier = Modifier.background(color = MaterialTheme.colors.background),
        navController = navController,
        startDestination = Navigation.root
    ){
        composable(Navigation.root){
            HomeScreen{
                when(it){
                    NavigationItem.HOME -> HistoryScreenPage(
                        onWorkoutClicked = {
                            navController.navigate(Navigation.Workout(it))
                        },
                        onAddClicked = {
                            navController.navigate(Navigation.Workout(null))
                        }
                    )
                    NavigationItem.EXERCISES -> ExerciseListScreenPage()
                    NavigationItem.WORKOUTS -> Unit
                    NavigationItem.STATS -> Unit
                    NavigationItem.PROFILE -> Unit
                }
            }
        }
        composable(
            route = Navigation.Workout.route,
            arguments = listOf(navArgument(Navigation.Workout.idArg) {
                type = NavType.LongType
            })
        ){ backStackEntry ->
            val id = backStackEntry.arguments?.getLong(Navigation.Workout.idArg)
            WorkoutScreenRoute(
                workoutId = id,
                onDeleted = { navController.navigateUp() }
            )
        }
    }
}