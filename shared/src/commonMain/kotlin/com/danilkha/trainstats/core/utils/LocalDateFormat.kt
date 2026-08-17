package com.danilkha.trainstats.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.danilkha.commoncore.utils.DateTimeFormatter

val LocalDateFormat = staticCompositionLocalOf<DateTimeFormatter> {
    error("LocalDateFormat was not provided. Wrap your composition in SharedApp() or provide LocalDateFormat manually.")
}

@Composable
expect fun rememberDateTimeFormatter(): DateTimeFormatter
