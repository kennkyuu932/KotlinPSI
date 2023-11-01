package com.example.kotlinpsi.Database

import androidx.room.TypeConverter
import java.sql.Date
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ContactConverter {
    @TypeConverter
    fun stringtolocaldatetime(value: String?): LocalDateTime?{
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return LocalDateTime.parse(value,formatter)
    }
    @TypeConverter
    fun localdatetimetostring(value: LocalDateTime): String?{
        return value.toString()
    }
}