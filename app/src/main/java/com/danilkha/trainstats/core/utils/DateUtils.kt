package com.danilkha.trainstats.core.utils

import korlibs.time.Date
import korlibs.time.DateTime
import korlibs.time.KlockLocale
import korlibs.time.PatternDateFormat
import korlibs.time.format
import korlibs.time.locale.russian


private val dateFormat = PatternDateFormat("EEEE, d MMMM", locale = KlockLocale.russian)
private val dateFormatYear = PatternDateFormat("EEEE, d MMMM yyyy")



fun Date.format(): String{
    val today = DateTime.now().date

    return if (year != today.year){
        dateFormatYear.format(this)
    }else{
        dateFormat.format(this)
    }
}


fun Date.asJavaDate() = java.util.Date(
    DateTime(
        date = this,
        time = DateTime.now().time
    ).unixMillisLong
)