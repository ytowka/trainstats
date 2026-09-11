package com.danilkha.navigation.api.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class WorkoutNav(
    val id: String?
) : NavKey
