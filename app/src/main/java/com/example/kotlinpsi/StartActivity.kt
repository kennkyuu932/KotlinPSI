package com.example.kotlinpsi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.kotlinpsi.ble.BLEService

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val startbutton=findViewById<Button>(R.id.button_start)

        startbutton.setOnClickListener {
//            val range=(0..10000)
//            val kamei = range.random()
            val intent = Intent(this,MainActivity::class.java)
//            intent.putExtra("kamei_activity",kamei)
            val service = Intent(this,BLEService::class.java)
//            service.putExtra("kamei_service",kamei)
            startService(service)
            startActivity(intent)
        }
    }

    companion object{
        val range = (0..10000)
        val kamei=range.random()
    }
}