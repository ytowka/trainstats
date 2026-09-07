package com.danilkha.trainstats.features.navigation

object Navigation {
    const val root = "root"

    const val settings = "settings"

    object Workout{
        const val idArg = "id"
        const val name = "workout"

        const val route = "$name?$idArg={$idArg}"

        operator fun invoke(id: String?): String{
            val validId = id ?: ""
            return "$name?$idArg=$validId"
        }
    }
}
