package com.danilkha.trainstats.features.navigation

object Navigation {
    const val root = "root"

    object Workout{
        const val idArg = "id"
        const val name = "workout"

        const val route = "$name?$idArg={$idArg}"

        operator fun invoke(id: String?): String{
            return if(id == null){
                name
            }else{
                "$name?$idArg=$id"
            }
        }
    }
}