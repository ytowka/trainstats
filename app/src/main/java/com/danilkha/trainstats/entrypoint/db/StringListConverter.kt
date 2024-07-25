package com.danilkha.trainstats.entrypoint.db

import androidx.room.TypeConverter

class StringListConverter {

    private val separator = ";;"

    @TypeConverter
    fun toString(list: List<String>): String{
        return list.joinToString(separator)
    }

    @TypeConverter
    fun toList(string: String): List<String>{
        return if(string.isNotBlank()){
            string.split(separator)
        }else emptyList()

    }
}