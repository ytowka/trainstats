package com.danilkha.trainstats.entrypoint.db

import androidx.room.TypeConverter
import korlibs.time.DateTime

class DateTimeConverter {

    @TypeConverter
    fun toDb(dateTime: DateTime?): Long?{
        return dateTime?.unixMillisLong
    }

    @TypeConverter
    fun toModel(unixMillis: Long?): DateTime?{
        return unixMillis?.let { DateTime(unixMillis) }
    }
}