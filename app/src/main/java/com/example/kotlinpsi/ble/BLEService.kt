package com.example.kotlinpsi.ble

import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.kotlinpsi.StartActivity


class BLEService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: service")
        //advertise
        BLEControl.startServer(application,StartActivity.kamei)
    }

    override fun onDestroy() {
        super.onDestroy()
        BLEControl.stopServer(application)
    }


    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "onBind: service")
        return null
    }
}