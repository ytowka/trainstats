package com.danilkha.trainstats

import androidx.compose.foundation.background
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.danilkha.home.HomeScreen
import com.danilkha.home.NavigationItem

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
                    NavigationItem.HOME -> Unit
                    NavigationItem.EXERCISES -> Unit
                    NavigationItem.WORKOUTS -> Unit
                    NavigationItem.STATS -> Unit
                    NavigationItem.PROFILE -> Unit
                }
            }
        }
    }
}