package com.example.kotlinpsi.Transmission

import android.content.ContentValues.TAG
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.kotlinpsi.Database.AddDataActivity
import com.example.kotlinpsi.Database.ContactDatabase
import com.example.kotlinpsi.Database.ContactViewModel
import com.example.kotlinpsi.R
import java.net.Inet4Address

class ServerActivity : AppCompatActivity() {

    val pri_key_len=32
    val ec_point_length=65

    private lateinit var viwmodel:ContactViewModel

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

        viwmodel = ViewModelProvider(this).get(ContactViewModel::class.java)
        viwmodel.mutabledata.observe(this, Observer { value ->
            if(value==0){
                Log.d(TAG, "onCreate: value = $value stop PSI")
                Toast.makeText(this,"データベースの取得に失敗",Toast.LENGTH_SHORT).show()
            }
            if(value==4){
                Log.d(TAG, "onCreate: value = $value start PSI")
            }
        })

        AsyncTask.execute {
            try {
                Log.d(TAG, "onCreate: start room")
                val db=ContactDatabase.getInstance(AddDataActivity())
                val roomlist=db.contactDao().getAllContacts()
                viwmodel.changeflag(4)
            }catch (_:Exception){
                Log.d(TAG, "onCreate: room get data miss")
                viwmodel.changeflag(0)
            }
        }

        Log.d(TAG, "onCreate: PSIStart")

        val pri_key_kt:ByteArray = ByteArray(pri_key_len)
        val f=createKey(pri_key_kt)

        val n=1 //要素数
        val test:String="test"
        val enc_mes:ByteArray=ByteArray(ec_point_length)
        val e=encryptSet(test,pri_key_kt, enc_mes)
        if(e) {
            Log.d(TAG, "onCreate: Back Kotlin finish encrypt message")
        }
    }


    external fun createKey(key :ByteArray): Boolean

    external fun encryptSet(message: String,key: ByteArray,out: ByteArray):Boolean




    companion object{
        init {
            System.loadLibrary("kotlinpsi")
        }
    }
}