package com.example.kotlinpsi.Transmission

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ServerViewModel : ViewModel() {

    val server_end_flag : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

//    val receiveflag_server : MutableLiveData<Int> by lazy {
//        MutableLiveData<Int>()
//    }
}