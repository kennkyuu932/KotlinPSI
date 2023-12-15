package com.example.kotlinpsi.Database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class ContactRepository(private val contactDao: ContactDao) {
    val allLists:Flow<List<Contact>> = contactDao.getAllContacts()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(contact: Contact){
        contactDao.insertContact(contact)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deletecontact(contact: Contact){
        contactDao.deleteContact(contact)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll(){
        contactDao.deleteContactAll()
    }


    fun SearchMonth(start: LocalDateTime,stop: LocalDateTime):Flow<List<Contact>>{
        return contactDao.SearchMonth(start,stop)
    }

    fun SearchName(name:ByteArray):Flow<List<Contact>>{
        return contactDao.SearchName(name)
    }


}