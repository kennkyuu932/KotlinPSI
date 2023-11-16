package com.example.kotlinpsi.Database

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ContactViewModel(private val repository: ContactRepository):ViewModel() {

    val allLists:LiveData<List<Contact>> = repository.allLists.asLiveData()

    fun insert(contact: Contact) = viewModelScope.launch {
        repository.insert(contact)
    }

    fun deletecontact(contact: Contact) = viewModelScope.launch {
        repository.deletecontact(contact)
    }

    fun deleteAll()=viewModelScope.launch {
        repository.deleteAll()
    }





//    private val _mutabledata=MutableLiveData<Int>()
//    val mutabledata:LiveData<Int>
//        get() = _mutabledata
//
//    fun changeflag(flag:Int){
//        AsyncTask.execute {
//            _mutabledata.postValue(flag)
//        }
//    }
//
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