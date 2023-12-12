package com.example.kotlinpsi.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ContactDao {
    @Query("SELECT * FROM Contact_history_table ORDER BY date")
    fun getAllContacts(): Flow<List<Contact>>

    @Insert
    suspend fun insertContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("DELETE FROM Contact_history_table")
    suspend fun deleteContactAll()

    @Query("SELECT * FROM Contact_history_table WHERE date BETWEEN :start AND :stop " +
            "ORDER BY date")
    fun SerachMonth(start:LocalDateTime,stop:LocalDateTime): LiveData<List<Contact>>

    @Query("SELECT * FROM Contact_history_table WHERE name = :name_s ORDER BY date")
    fun SearchName(name_s:ByteArray):LiveData<List<Contact>>

//    @Query("SELECT * FROM Contact_history_table WHERE date BETWEEN :start AND :stop " +
//            "ORDER BY date")
//    fun SearchRange(start: LocalDateTime,stop: LocalDateTime):LiveData<List<Contact>>

//    @Query("SELECT * FROM Contact_history_table")
//    fun getAllList(): List<Contact>
}