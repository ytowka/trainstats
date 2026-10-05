package com.danilkha.commoncore.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberDateTimeFormatter(): DateTimeFormatter = remember {
    createDateTimeFormatter(localeTag = "en-US", use24HourFormat = true)
}
