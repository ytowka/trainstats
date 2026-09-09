package com.danilkha.trainstats

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import com.danilkha.commonds.theme.TrainingStatsTheme
import com.danilkha.commoncore.utils.LocalDateFormat
import com.danilkha.commoncore.utils.rememberDateTimeFormatter
import com.danilkha.trainstats.features.navigation.RootScreen

@Composable
fun SharedApp() {
    TrainingStatsTheme {
        val formatter = rememberDateTimeFormatter()
        CompositionLocalProvider(
            LocalDateFormat provides formatter,
        ) {
            RootScreen()
        }
    }
}
