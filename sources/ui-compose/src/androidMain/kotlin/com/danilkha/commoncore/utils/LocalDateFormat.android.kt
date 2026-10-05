package com.danilkha.commoncore.utils

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberDateTimeFormatter(): DateTimeFormatter {
    val context = LocalContext.current
    val localeTag = context.resources.configuration.locales[0].toLanguageTag()
    val use24HourFormat = DateFormat.is24HourFormat(context)
    return remember(localeTag, use24HourFormat) {
        createDateTimeFormatter(localeTag, use24HourFormat)
    }
}
