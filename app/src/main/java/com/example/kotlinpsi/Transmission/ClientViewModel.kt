package com.example.kotlinpsi.Transmission

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

class ClientViewModel():ViewModel() {

    val receiveflag: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
}