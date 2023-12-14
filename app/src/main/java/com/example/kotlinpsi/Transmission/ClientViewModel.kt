package com.example.kotlinpsi.Transmission

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ClientViewModel():ViewModel() {
    val _client_encrypt_list = MutableLiveData<List<List<ByteArray>>>()
    val client_encrypt_list :LiveData<List<List<ByteArray>>> get() = _client_encrypt_list
}