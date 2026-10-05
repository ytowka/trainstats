package com.danilkha.commoncore.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

actual fun createDateTimeFormatter(
    localeTag: String,
    use24HourFormat: Boolean,
): DateTimeFormatter = object : DateTimeFormatter {
    override fun format(date: LocalDate): String = date.toString()

    override fun format(dateTime: LocalDateTime): String = dateTime.toString()

    override fun is24hourFormat(): Boolean = use24HourFormat
}
