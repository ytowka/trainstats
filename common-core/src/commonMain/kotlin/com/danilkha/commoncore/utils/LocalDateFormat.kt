package com.danilkha.commoncore.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalDateFormat = staticCompositionLocalOf<DateTimeFormatter> {
    error("LocalDateFormat was not provided. Wrap your composition in SharedApp() or provide LocalDateFormat manually.")
}

@Composable
expect fun rememberDateTimeFormatter(): DateTimeFormatter
