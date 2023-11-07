package com.example.kotlinpsi.Database

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ContactViewModel:ViewModel() {
    private val _mutabledata=MutableLiveData<Int>()
    val mutabledata:LiveData<Int>
        get() = _mutabledata

    fun changeflag(flag:Int){
        AsyncTask.execute {
            _mutabledata.postValue(flag)
        }
    }
}