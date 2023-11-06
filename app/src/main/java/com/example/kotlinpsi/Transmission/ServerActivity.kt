package com.example.kotlinpsi.Transmission

import android.content.ContentValues.TAG
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.kotlinpsi.R
import java.net.Inet4Address

class ServerActivity : AppCompatActivity() {

    val pri_key_len=32
    val ec_point_length=16
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)

        val server_ip_text=findViewById<TextView>(R.id.server_ip)

        val connectivityManager = getSystemService(ConnectivityManager::class.java)

        val networkCallback = object : ConnectivityManager.NetworkCallback(){
            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties)
                server_ip_text.text=linkProperties.linkAddresses.filter {
                    it.address is Inet4Address
                }[0].toString()
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        Log.d(TAG, "onCreate: PSIStart")

        val pri_key_kt:ByteArray = ByteArray(pri_key_len)
        val f=createKey(pri_key_kt)
        val n=1 //要素数
        val test:String="test"
        val enc_mes:ByteArray=ByteArray(ec_point_length)
        val e=encryptSet(test,pri_key_kt, enc_mes)
        Log.d(TAG, "onCreate: Back Kotlin finish encrypt message")
//        ServerFirstPSI(test)
    }

//    fun FirstAfter(data_first_enc:ByteArray,data_first_len:IntArray){
//        Log.d(TAG, "FirstAfter: Start")
//    }

    external fun createKey(key :ByteArray): Boolean

    external fun encryptSet(message: String,key: ByteArray,out: ByteArray):Boolean




//    external fun ServerFirstPSI(text:String)


    companion object{
        init {
            System.loadLibrary("kotlinpsi")
        }
    }
}