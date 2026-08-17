package com.danilkha.trainstats.features.navigation

object Navigation {
    const val root = "root"

    const val settings = "settings"

    object Workout{
        const val idArg = "id"
        const val name = "workout"

        const val route = "$name?$idArg={$idArg}"

        operator fun invoke(id: Long?): String{
            val validId = id ?: -1
            return "$name?$idArg=$validId"
        }
    }
}
