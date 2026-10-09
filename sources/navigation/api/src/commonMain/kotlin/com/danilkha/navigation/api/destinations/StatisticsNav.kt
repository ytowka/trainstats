package com.danilkha.navigation.api.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object StatisticsNav : NavKey

@Serializable
data class WorkoutDetailsNav(val id: String) : NavKey
