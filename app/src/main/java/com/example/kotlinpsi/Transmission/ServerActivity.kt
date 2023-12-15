package com.example.kotlinpsi.Transmission

import android.content.ContentValues.TAG
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.kotlinpsi.Database.ContactApplication
import com.example.kotlinpsi.Database.ContactViewModel
import com.example.kotlinpsi.R
import java.net.Inet4Address
import androidx.lifecycle.observe
import com.example.kotlinpsi.Database.Contact
import com.example.kotlinpsi.MainActivity
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ServerActivity : AppCompatActivity() {

    val pri_key_len=32
    val ec_point_length=65


    val enc_mes_list= mutableListOf<List<ByteArray>>()

    private val contactViewModel:ContactViewModel by viewModels {
        ContactViewModel.ContactViewmodelFactory((application as ContactApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)

        val server_ip_text=findViewById<TextView>(R.id.server_ip)

        val radioflag=intent.getIntExtra(MainActivity.radioflag,0)

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

        val pri_key_kt:ByteArray = ByteArray(pri_key_len)
        val f=createKey(pri_key_kt)

        when (radioflag) {
            0 -> {
                Log.d(TAG, "onCreate: 全部のデータでPSI")
                contactViewModel.allLists.observe(this) { contacts ->
                    Toast.makeText(this, "PSI開始", Toast.LENGTH_SHORT).show()

                    Log.d(TAG, "onCreate: PSIStart ")

                    Log.d(TAG, "onCreate: PSI step1")
                    contacts.let {
                        PSIencrypt(contacts, pri_key_kt)
                        PSISend(enc_mes_list)
                    }
                }
            }
            1->{
                Log.d(TAG, "onCreate: 現時点から1ヶ月分のデータでPSI")
                val now = LocalDateTime.now()
                val start:LocalDateTime
                if(now.monthValue==1){
                    start= LocalDateTime.of(now.year-1,12,now.dayOfMonth,now.hour,now.minute)
                }else{
                    start=LocalDateTime.of(now.year,now.monthValue-1,now.dayOfMonth,now.hour,now.minute)
                }
                contactViewModel.SearchMonth(start,now).asLiveData().observe(owner = this){
                    contacts -> contacts.let {
                        Log.d(TAG, "onCreate: PSIStart")
                        Log.d(TAG, "onCreate: ${contacts.size}")
                        PSIencrypt(contacts, pri_key_kt)
                        PSISend(enc_mes_list)
                    }
                }
            }
            3->{
                Log.d(TAG, "onCreate: 現時点から3ヶ月分のデータでPSI")
                val now = LocalDateTime.now()
                val start:LocalDateTime
                if(now.monthValue==3){
                    start= LocalDateTime.of(now.year-1,12,now.dayOfMonth,now.hour,now.minute)
                }else if(now.monthValue==2){
                    start= LocalDateTime.of(now.year-1,11,now.dayOfMonth,now.hour,now.minute)
                } else if(now.monthValue==1){
                    start= LocalDateTime.of(now.year-1,10,now.dayOfMonth,now.hour,now.minute)
                }else{
                    start=LocalDateTime.of(now.year,now.monthValue-3,now.dayOfMonth,now.hour,now.minute)
                }
                contactViewModel.SearchMonth(start,now).asLiveData().observe(this){
                        contacts -> contacts.let {
                            Log.d(TAG, "onCreate: PSIStart")
                            Log.d(TAG, "onCreate: ${contacts.size}")
                            PSIencrypt(contacts, pri_key_kt)
                            PSISend(enc_mes_list)
                        }
                }
            }
        }


    }

    fun PSIencrypt(contacts:List<Contact>,pri_key_kt: ByteArray){
        Log.d(TAG, "onCreate: ${contacts}")

        var i=1 //すべての要素を暗号化できているかのテスト用変数

        for (message in contacts){
            var j=1 //テスト用変数
            val enc_result= mutableListOf<ByteArray>()
            for (name in message.name){
                Log.d(TAG, "onCreate: encrypt name : $name")
                val enc_mes:ByteArray=ByteArray(ec_point_length)
                val e=encryptSet(name,pri_key_kt,enc_mes)
                enc_result.add(enc_mes)
                if(e){
                    Log.d(TAG, "onCreate: Back Kotlin finish encrypt name No.$i,$j")
                }
                j++
            }
            enc_mes_list.add(enc_result)
            Log.d(TAG, "onCreate: encrypt message No.$i finish")
            i++
        }

        Log.d(TAG, "onCreate: PSI finish step1 No.${contacts.size}")
    }

    fun PSISend(encmes:List<List<ByteArray>>){
        Log.d(TAG, "onCreate: send my encrypt data to client")
        Toast.makeText(this,"通信開始(Server to Client)",Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            Log.d(TAG, "onCreate: Start connect to client")
            Control.ServerConnect()
            Log.d(TAG, "onCreate: Send encrypt message to Client")
            Control.ServerSend(enc_mes_list)
            Log.d(TAG, "onCreate: Start Receive client encrypt message")
            Control.ServerReceive()
        }
    }



    external fun createKey(key :ByteArray): Boolean

    external fun encryptSet(message: Byte, key: ByteArray, out: ByteArray):Boolean


    external fun aestest():Boolean


    companion object{
        init {
            System.loadLibrary("kotlinpsi")
        }
    }
}