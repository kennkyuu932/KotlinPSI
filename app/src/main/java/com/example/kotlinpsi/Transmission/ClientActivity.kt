package com.example.kotlinpsi.Transmission

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.kotlinpsi.MainActivity
import com.example.kotlinpsi.R
import kotlinx.coroutines.launch

class ClientActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        val ipaddr=intent.getStringExtra(MainActivity.server_ip)

        lifecycleScope.launch {
            if (ipaddr!=null){
                Control.ClientConnect(ipaddr)
                while (true){
                    Control.ClientReceiveMessage()
                }
            }
        }
    }
}