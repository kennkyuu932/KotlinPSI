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

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun SearchMonth(start:LocalDateTime,stop:LocalDateTime): LiveData<List<Contact>> {
        return contactDao.SerachMonth(start,stop)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun SearchName(name:ByteArray): LiveData<List<Contact>> {
        return contactDao.SearchName(name)
    }

//    @Suppress("RedundantSuspendModifier")
//    @WorkerThread
//    suspend fun SearchRange(first_year:Int, first_month: Int ,second_year:Int, second_month:Int): LiveData<List<Contact>>{
//        return contactDao.SearchRange(first_year,first_month,second_year,second_month)
//    }

}