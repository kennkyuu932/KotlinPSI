package com.example.kotlinpsi.Database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

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

}