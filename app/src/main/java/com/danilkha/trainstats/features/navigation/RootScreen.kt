package com.danilkha.trainstats.features.navigation

import androidx.compose.foundation.background
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.danilkha.trainstats.features.exercises.ui.ExerciseListScreen
import com.danilkha.trainstats.features.exercises.ui.ExerciseListScreenPage
import com.danilkha.trainstats.features.history.ui.HistoryScreenPage
import com.danilkha.trainstats.features.home.ui.HomeScreen
import com.danilkha.trainstats.features.home.ui.NavigationItem
import com.danilkha.trainstats.features.workout.ui.WorkoutScreenRoute

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
                    NavigationItem.HOME -> HistoryScreenPage(onAddClicked = {
                        navController.navigate(Navigation.Workout(null))
                    })
                    NavigationItem.EXERCISES -> ExerciseListScreenPage()
                    NavigationItem.WORKOUTS -> Unit
                    NavigationItem.STATS -> Unit
                    NavigationItem.PROFILE -> Unit
                }
            }
        }
        composable(Navigation.Workout.route){
            WorkoutScreenRoute()
        }
    }
}