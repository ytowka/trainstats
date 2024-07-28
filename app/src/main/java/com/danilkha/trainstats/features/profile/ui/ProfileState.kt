package com.danilkha.trainstats.features.profile.ui

import androidx.compose.ui.text.TextRange

data class ProfileState(
    val exportText: String = "",
    val isLoading: Boolean = false,
    val errorLine: TextRange? = null
) {

}

sealed interface ProfileSingleEvent{
    class ImportSuccess(val exercises: Int, val workouts: Int) : ProfileSingleEvent
    object Error : ProfileSingleEvent
}