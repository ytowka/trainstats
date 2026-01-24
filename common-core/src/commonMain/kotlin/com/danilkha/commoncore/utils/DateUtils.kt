package com.danilkha.commoncore.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime


fun Instant(millisecondsLong: Long): Instant {
    return Instant.fromEpochMilliseconds(millisecondsLong)
}

fun Instant.toLocal(): LocalDateTime {
    val timeZone = TimeZone.currentSystemDefault()
    return toLocalDateTime(timeZone)
}

fun Instant.asLocal(): LocalDateTime {
    return toLocalDateTime(TimeZone.UTC)
}

fun Instant.toLocalDate(): LocalDate {
    val timeZone = TimeZone.currentSystemDefault()
    return toLocalDateTime(timeZone).date
}


val LocalDateTime.millisecondsLong
    get() = this.toInstant(UtcOffset.ZERO).toEpochMilliseconds()
