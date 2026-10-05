package com.danilkha.commoncore.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter as JavaDateTimeFormatter
import java.util.Locale
import kotlin.time.Clock

private class JvmDateTimeFormatter(
    localeTag: String,
    private val use24HourFormat: Boolean,
) : DateTimeFormatter {
    private val locale = Locale.forLanguageTag(localeTag)
    private val dateFormat = JavaDateTimeFormatter.ofPattern("EEEE, d MMMM", locale)
    private val dateFormatYear = JavaDateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", locale)
    private val time24Format = JavaDateTimeFormatter.ofPattern("HH:mm", locale)
    private val time12Format = JavaDateTimeFormatter.ofPattern("h:mm a", Locale.US)

    override fun format(date: LocalDate): String {
        val today = Clock.System.now().toLocalDate()
        return if (date.year == today.year) {
            dateFormat.format(date.toJavaLocalDate())
        } else {
            dateFormatYear.format(date.toJavaLocalDate())
        }
    }

    override fun format(dateTime: LocalDateTime): String {
        val timeFormat = if (use24HourFormat) time24Format else time12Format
        return "${format(dateTime.date)} • ${timeFormat.format(dateTime.toJavaLocalDateTime())}"
    }

    override fun is24hourFormat(): Boolean = use24HourFormat
}

actual fun createDateTimeFormatter(
    localeTag: String,
    use24HourFormat: Boolean,
): DateTimeFormatter = JvmDateTimeFormatter(localeTag, use24HourFormat)
