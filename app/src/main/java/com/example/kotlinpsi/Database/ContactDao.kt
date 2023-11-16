package com.example.kotlinpsi.Database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM Contact_history_table")
    fun getAllContacts(): Flow<List<Contact>>

    @Insert
    suspend fun insertContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("DELETE FROM Contact_history_table")
    suspend fun deleteContactAll()

//    @Query("SELECT * FROM Contact_history_table")
//    fun getAllList(): List<Contact>
}