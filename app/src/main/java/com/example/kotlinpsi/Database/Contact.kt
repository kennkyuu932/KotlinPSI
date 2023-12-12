package com.example.kotlinpsi.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.time.LocalDateTime

@Entity(tableName = "Contact_history_table", primaryKeys = ["date" , "name"])
data class Contact(
    @ColumnInfo(name = "date") val date: LocalDateTime,
    @ColumnInfo(name = "name") val name: ByteArray
)
