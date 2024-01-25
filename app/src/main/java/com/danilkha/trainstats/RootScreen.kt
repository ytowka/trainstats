package com.danilkha.trainstats

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun RootScreen() {

    val navController = rememberNavController()
    NavHost(navController, startDestination = Navigation.root){
        composable(Navigation.root){

        }
    }
}