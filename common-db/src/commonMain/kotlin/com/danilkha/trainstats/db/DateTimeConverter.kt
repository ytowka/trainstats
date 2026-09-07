package com.danilkha.trainstats.db

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

class DateTimeConverter {

    @TypeConverter
    fun toDb(instant: Instant?): Long?{
        return instant?.toEpochMilliseconds()
    }

    @TypeConverter
    fun toModel(unixMillis: Long?): Instant?{
        return unixMillis?.let { Instant.fromEpochMilliseconds(it) }
    }
}
