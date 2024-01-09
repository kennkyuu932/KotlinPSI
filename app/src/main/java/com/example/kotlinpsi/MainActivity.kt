package com.example.kotlinpsi

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import com.example.kotlinpsi.Database.AddDataActivity
import com.example.kotlinpsi.Transmission.ClientActivity
import com.example.kotlinpsi.Transmission.ServerActivity
import com.example.kotlinpsi.databinding.ActivityMainBinding
import java.net.Inet4Address

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        //binding.sampleText.text = stringFromJNI()
        //Log.d(TAG, "Boringtest: "+Boringtest())

        val server_ip_text_main=findViewById<TextView>(R.id.server_ip_main)
        var radio=0

        val connectivityManager = getSystemService(ConnectivityManager::class.java)

        val networkCallback = object : ConnectivityManager.NetworkCallback(){
            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties)
                server_ip_text_main.text=linkProperties.linkAddresses.filter {
                    it.address is Inet4Address
                }[0].toString()
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

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
            //Log.d(TAG, "onCreate: push server psi button")
            val intent=Intent(this,ServerActivity::class.java)
            intent.putExtra(radioflag,radio)
            startActivity(intent)
        }
        val clibutton=binding.clientPsi
        clibutton.setOnClickListener {
            //Log.d(TAG, "onCreate: push client psi button")
            val intent=Intent(this,ClientActivity::class.java)
            intent.putExtra(server_ip,ip_text.text.toString())
            intent.putExtra(radioflag,radio)
            startActivity(intent)
        }


        val radio1month=binding.radioMonthlater
        radio1month.setOnClickListener {
            //Log.d(TAG, "onCreate: radio 1")
            radio=1
        }
        val radio3month=binding.radio3monthlater
        radio3month.setOnClickListener {
            //Log.d(TAG, "onCreate: radio 3")
            radio=3
        }
        val radioall=binding.radioAll
        radioall.setOnClickListener {
            //Log.d(TAG, "onCreate: radio 0")
            radio=0
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



    companion object {
        // Used to load the 'kotlinpsi' library on application startup.
        init {
            System.loadLibrary("kotlinpsi")
        }
        val server_intent_message = "SERVRMESSAGE"
        val client_intent_message = "CLIENTMESSAGE"
        val psi_intent_message = "PSIRESULT"

        val server_ip = "SERVERIP"
        val radioflag = "RADIOBUTTON"

        //時間計測のためのTAG
        val TAG_TIME = "TIME_to_PSI"

        var encrypt_start_first = 0L
        var encrypt_finish_first = 0L
        var send_start_first = 0L
        var send_finish_first = 0L
        var receive_start_first = 0L
        var receive_finish_first = 0L
        var encrypt_start_second = 0L
        var encrypt_finish_second = 0L
        var send_start_second = 0L
        var send_finish_second = 0L
        var receive_start_second = 0L
        var receive_finish_second = 0L

        public lateinit var test:String
    }
}