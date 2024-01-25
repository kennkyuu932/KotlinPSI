package com.example.kotlinpsi.ble

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BLEViewModel: ViewModel() {
    val bleflag : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
}