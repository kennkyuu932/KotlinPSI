package com.example.kotlinpsi.Database

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ContactConverter {
    @TypeConverter
    fun stringtolocaldatetime(value: String?): LocalDateTime?{
        try {
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            return LocalDateTime.parse(value,formatter)
        }catch (_:Exception){
            try {
                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                return LocalDateTime.parse(value,formatter)
            }catch (_:Exception){
                return null
            }
        }
    }
    @TypeConverter
    fun localdatetimetostring(value: LocalDateTime): String?{
        return value.toString()
    }
}