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
import com.example.kotlinpsi.Database.ContactApplication
import com.example.kotlinpsi.Database.ContactViewModel
import com.example.kotlinpsi.R
import java.net.Inet4Address
import androidx.lifecycle.observe

class ServerActivity : AppCompatActivity() {

    val pri_key_len=32
    val ec_point_length=65

//    private lateinit var viewmodel:ContactViewModel

//    lateinit var roomlist:Flow<List<Contact>>
//
//    lateinit var roomlist2:List<Contact>

    private val contactViewModel:ContactViewModel by viewModels {
        ContactViewModel.ContactViewmodelFactory((application as ContactApplication).repository)
    }

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

//        viewmodel = ViewModelProvider(this).get(ContactViewModel::class.java)
//        viewmodel.mutabledata.observe(this, Observer { value ->
//            if(value==0){
//                Log.d(TAG, "onCreate: value = $value stop PSI")
//                Toast.makeText(this,"データベースの取得に失敗",Toast.LENGTH_SHORT).show()
//            }
//            if(value==4){
//                Log.d(TAG, "onCreate: value = $value start PSI")
//
//
//            }
//        })

//        val db=ContactDatabase.getInstance(this)
//
//        lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                Log.d(TAG, "onCreate: start room")
////                roomlist=db.contactDao().getAllContacts()
//                //roomlist2=db.contactDao().getAllList()
//                viewmodel.changeflag(4)
//            }catch (_:Exception){
//                Log.d(TAG, "onCreate: room get data miss")
//                viewmodel.changeflag(0)
//            }
//        }

        contactViewModel.allLists.observe(owner = this){
            contacts -> contacts.let {
                Log.d(TAG, "onCreate: ${contacts.toString()}")

                Toast.makeText(this,"PSI開始",Toast.LENGTH_SHORT).show()

                Log.d(TAG, "onCreate: PSIStart ")

                Log.d(TAG, "onCreate: PSI step1")

                val pri_key_kt:ByteArray = ByteArray(pri_key_len)
                val f=createKey(pri_key_kt)
                var i=1 //すべての要素を暗号化できているかのテスト用変数

                for (message in contacts){
//                    val n=1 //要素数
                    Log.d(TAG, "PSI loop step1: No.${i}")
                    val test:String=message.toString()
                    Log.d(TAG, "encryptmessage: ${message.toString()}")
                    val enc_mes:ByteArray=ByteArray(ec_point_length)
                    val e=encryptSet(test,pri_key_kt, enc_mes)
                    if(e) {
                        Log.d(TAG, "onCreate: Back Kotlin finish encrypt message")
                    }
                    i=i+1
                }
                Log.d(TAG, "onCreate: PSI finish step1 No.${contacts.size}")
            }
        }


    }


    external fun createKey(key :ByteArray): Boolean

    external fun encryptSet(message: String,key: ByteArray,out: ByteArray):Boolean


    external fun aestest():Boolean


    companion object{
        init {
            System.loadLibrary("kotlinpsi")
        }
    }
}