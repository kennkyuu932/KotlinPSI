package com.example.kotlinpsi.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.time.LocalDateTime

@Entity(tableName = "Contact_history_table", primaryKeys = ["date" , "name"])
data class Contact(
//    @PrimaryKey @ColumnInfo(name="date") val date:String,
//    @ColumnInfo(name="time") val time:String,
//    @ColumnInfo(name = "name") val name:String
//    @PrimaryKey @ColumnInfo(name = "date") val date: LocalDateTime,
//    //@PrimaryKey @ColumnInfo(name = "time") val time: Time,
//    @PrimaryKey @ColumnInfo(name = "name") val name: String
    @ColumnInfo(name = "date") val date: LocalDateTime,
    @ColumnInfo(name = "name") val name: String
)
