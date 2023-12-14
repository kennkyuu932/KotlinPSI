package com.example.kotlinpsi.Database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ContactViewModel(private val repository: ContactRepository):ViewModel() {

    val allLists:LiveData<List<Contact>> = repository.allLists.asLiveData()

    val _monthLists = MutableLiveData<LiveData<List<Contact>>>()
    val monthLists : LiveData<LiveData<List<Contact>>> get()=_monthLists

    val _NameLists = MutableLiveData<LiveData<List<Contact>>>()
    val NameLists : LiveData<LiveData<List<Contact>>> get()=_NameLists

//    val _RangeLists = MutableLiveData<LiveData<List<Contact>>>()
//    val RangeLists : LiveData<LiveData<List<Contact>>> get()=_RangeLists

    fun insert(contact: Contact) = viewModelScope.launch {
        repository.insert(contact)
    }

    fun deletecontact(contact: Contact) = viewModelScope.launch {
        repository.deletecontact(contact)
    }

    fun deleteAll()=viewModelScope.launch {
        repository.deleteAll()
    }

    fun SearchMonth(start:LocalDateTime,stop:LocalDateTime)=viewModelScope.launch {
        _monthLists.value=repository.SearchMonth(start,stop)
    }

    fun SearchName(name : ByteArray)=viewModelScope.launch {
        _NameLists.value=repository.SearchName(name)
    }



    class ContactViewmodelFactory(private val repository: ContactRepository) : ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(ContactViewModel::class.java)){
                @Suppress("UNCHECKED_CAST")
                return ContactViewModel(repository) as T
            }
            throw IllegalArgumentException("Unkonw ViewModel class")
        }
    }
}