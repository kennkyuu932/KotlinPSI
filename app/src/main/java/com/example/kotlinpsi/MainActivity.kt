package com.example.kotlinpsi

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import com.example.kotlinpsi.Database.AddDataActivity
import com.example.kotlinpsi.Transmission.ClientActivity
import com.example.kotlinpsi.Transmission.ServerActivity
import com.example.kotlinpsi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        //binding.sampleText.text = stringFromJNI()
        //Log.d(TAG, "Boringtest: "+Boringtest())

        val database = binding.toAdddata
        database.setOnClickListener {
            val intent=Intent(this, AddDataActivity::class.java)
            startActivity(intent)
        }

        val psibutton=binding.psiStart
        psibutton.setOnClickListener {
            val server_mes=binding.serverMessage.text.toString()
            val client_mes=binding.clientMessage.text.toString()
            val result=OneCryptoMessage(server_mes,client_mes)
            val intent: Intent = Intent(this,PSIAfterActivity::class.java)
            intent.putExtra(server_intent_message,server_mes)
            intent.putExtra(client_intent_message,client_mes)
            intent.putExtra(psi_intent_message,result)
            startActivity(intent)
        }

        val ip_text=findViewById<EditText>(R.id.edit_ip)

        val serbutton=binding.serverPsi
        serbutton.setOnClickListener {
            Log.d(TAG, "onCreate: push server psi button")
            val intent=Intent(this,ServerActivity::class.java)
            startActivity(intent)
        }
        val clibutton=binding.clientPsi
        clibutton.setOnClickListener {
            Log.d(TAG, "onCreate: push client psi button")
            val intent=Intent(this,ClientActivity::class.java)
            intent.putExtra(server_ip,ip_text.text)
            startActivity(intent)
        }
    }

    /**
     * A native method that is implemented by the 'kotlinpsi' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun Boringtest(): Int

    //1端末でPSIのデモ
    external fun OneCryptoMessage(message:String,message_cl:String): String

//    //2端末間での通信(サーバ側)
//    external fun Socket_Server(message: String): Int
//
//    //2端末間での通信(クライアント側)
//    //クライアント側は常時実行される必要がある?
//    external fun Socket_Client(message: String): Int


    companion object {
        // Used to load the 'kotlinpsi' library on application startup.
        init {
            System.loadLibrary("kotlinpsi")
        }
        val server_intent_message = "SERVRMESSAGE"
        val client_intent_message = "CLIENTMESSAGE"
        val psi_intent_message = "PSIRESULT"

        val server_ip = "SERVERIP"


        public lateinit var test:String
    }
}