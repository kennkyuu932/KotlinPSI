package com.example.kotlinpsi

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Contact_history_table")
data class Contact(
    @PrimaryKey @ColumnInfo(name="date") val date:String,
    @ColumnInfo(name="time") val time:String,
    @ColumnInfo(name = "name") val name:String
)
