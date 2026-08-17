package com.danilkha.trainstats.core.utils

import androidx.compose.runtime.Composable
import com.danilkha.commoncore.utils.DateTimeFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

@Composable
actual fun rememberDateTimeFormatter(): DateTimeFormatter = object : DateTimeFormatter {
    override fun format(date: LocalDate): String = date.toString()
    override fun format(dateTime: LocalDateTime): String = dateTime.toString()
    override fun is24hourFormat(): Boolean = true
}
