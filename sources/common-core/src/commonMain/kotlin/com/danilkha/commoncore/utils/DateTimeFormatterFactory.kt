package com.danilkha.commoncore.utils

expect fun createDateTimeFormatter(
    localeTag: String,
    use24HourFormat: Boolean,
): DateTimeFormatter
