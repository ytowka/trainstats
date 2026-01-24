package com.danilkha.trainstats.core.utils

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.staticCompositionLocalOf
import com.danilkha.commoncore.utils.DateTimeFormatter
import com.danilkha.commoncore.utils.toLocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import java.util.Locale


class JvmDateTimeFormatter(private val context: Context) : DateTimeFormatter {

    private val currentLocale = context.resources.configuration.locales[0]
    private val dateFormat = java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM", currentLocale)
    private val dateFormatYear = java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", currentLocale)
    private val time24Format = java.time.format.DateTimeFormatter.ofPattern("HH:mm", currentLocale)
    private val time12Format = java.time.format.DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    @OptIn(FormatStringsInDatetimeFormats::class)
    override fun format(date: LocalDate): String {
        val today = Clock.System.now().toLocalDate()

        return if(date.year == today.year) {
            dateFormat.format(date.toJavaLocalDate())
        } else {
            dateFormatYear.format(date.toJavaLocalDate())
        }
    }

    override fun format(dateTime: LocalDateTime): String {
        val dateStr = format(dateTime.date)

        val timeStr = if(is24hourFormat()) {
            time24Format.format(dateTime.toJavaLocalDateTime())
        } else {
            time12Format.format(dateTime.toJavaLocalDateTime())
        }
        return "$dateStr • $timeStr"
    }

    override fun is24hourFormat(): Boolean {
        return DateFormat.is24HourFormat(context)
    }
}

val LocalDateFormat = staticCompositionLocalOf<DateTimeFormatter> { throw NotImplementedError() }
