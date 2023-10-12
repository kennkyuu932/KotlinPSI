package com.example.kotlinpsi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class PSIAfterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_psiafter)

        val ser_mes = findViewById<TextView>(R.id.server_mes)
        val cli_mes = findViewById<TextView>(R.id.client_mes)
        val psi_mes = findViewById<TextView>(R.id.psi_mes)

        val result = intent.getStringExtra(MainActivity.psi_intent_message)
        val s_mes = intent.getStringExtra(MainActivity.server_intent_message)
        val c_mes = intent.getStringExtra(MainActivity.client_intent_message)
        ser_mes.setText(s_mes)
        cli_mes.setText(c_mes)
        psi_mes.setText(result)
    }
}