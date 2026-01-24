package com.danilkha.commoncore.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime


interface DateTimeFormatter {

    fun format(date: LocalDate): String

    fun format(dateTime: LocalDateTime): String

    fun is24hourFormat(): Boolean
}
