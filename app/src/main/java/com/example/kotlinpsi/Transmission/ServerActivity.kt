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
import androidx.lifecycle.Observer
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
import kotlin.math.log

class ServerActivity : AppCompatActivity() {

    val pri_key_len=32
    val ec_point_length=65



    //自分の集合を暗号化したもの
    val enc_mes_list= mutableListOf<List<ByteArray>>()

    //クライアントから受け取った集合を暗号化したもの
    val enc_mes_double = mutableListOf<List<ByteArray>>()

    private val contactViewModel:ContactViewModel by viewModels {
        ContactViewModel.ContactViewmodelFactory((application as ContactApplication).repository)
    }

    private val serverviewmodel:ServerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)

        val server_ip_text=findViewById<TextView>(R.id.server_ip)

        val radioflag=intent.getIntExtra(MainActivity.radioflag,0)

        val pri_key_kt:ByteArray = ByteArray(pri_key_len)
        val f=createKey(pri_key_kt)

        //クライアントから受け取った暗号データ
        val ser_res_list_first= mutableListOf<List<ByteArray>>()

        val mserendflagObserver = Observer<Int>{ flag ->
            when(flag){
                1->{
                    //Log.d(TAG, "onCreate: observer start flag : ${Control.ser_end_flag} = $flag ?")
                    Toast.makeText(this,"finish step1",Toast.LENGTH_SHORT).show()

                    //受け取り(step2)
                    lifecycleScope.launch {
                        var i=0
                        Control.ServerReceiveSize()
                        val firstlistsize=Control.ser_res_size
                        Control.ser_res_size?.let { Control.ServerSendNotice(it) }
                        Log.d(TAG, "onCreate: Outside loop Server ${firstlistsize}")
                        while (i<firstlistsize!!){
                            Control.ser_res_size=null
                            Control.ServerReceiveSize()
                            val ser_res_encrypt_second = mutableListOf<ByteArray>()
                            var j=0
                            val secondlistsize = Control.ser_res_size
                            Control.ser_res_size?.let { Control.ServerSendNotice(it) }
                            while (j<secondlistsize!!){
                                Control.ser_res_size=null
                                Control.ServerReceiveSize()
                                val thirdlistsize=Control.ser_res_size
                                Log.d(TAG, "onCreate: thirdlistsize $thirdlistsize")
                                Control.ser_res_size?.let { Control.ServerSendNotice(it) }
                                if (thirdlistsize!=null){
                                    Control.ServerReceive(thirdlistsize)
                                }
                                j++
                                Control.ser_res_mes.let { ser_res_encrypt_second.add(it) }
                                val res_size=Control.ser_res_mes.size
                                Control.ServerSendNotice(res_size)
                            }
                            i++
                            ser_res_list_first.add(ser_res_encrypt_second)
                        }
                        Control.ServerSendNotice(2)
                        serverviewmodel.server_end_flag.value=2
                    }
                }
                2->{
                    //step3 クライアントからもらった集合を暗号化して送る．
                    Toast.makeText(this,"start step3",Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onCreate: start step3")
                    Log.d(TAG, "onCreate: receive list size ${ser_res_list_first.size}")
                    PSIdoubleencrypt(ser_res_list_first,pri_key_kt)
                    Log.d(TAG, "onCreate: double encrypt success")
                    Log.d(TAG, "onCreate: enc double size ${enc_mes_double.size}")
                    PSISendSecond(enc_mes_double,serverviewmodel)
                }
                3->{
                    //step4 クライアントから共通部分の場所をもらう
                    Toast.makeText(this,"start step4",Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onCreate: start step4")
                }
            }


        }

        serverviewmodel.server_end_flag.observe(this,mserendflagObserver)

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





        when (radioflag) {
            0 -> {
                Log.d(TAG, "onCreate: 全部のデータでPSI")
                contactViewModel.allLists.observe(this) { contacts ->
                    Toast.makeText(this, "PSI開始", Toast.LENGTH_SHORT).show()

                    Log.d(TAG, "onCreate: PSIStart ")

                    Log.d(TAG, "onCreate: PSI step1")
                    contacts.let {
                        PSIencrypt(contacts, pri_key_kt)
                        PSISend_first(enc_mes_list,serverviewmodel)
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
                        PSISend_first(enc_mes_list,serverviewmodel)
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
                            PSISend_first(enc_mes_list,serverviewmodel)
                        }
                }
            }
        }

//
//        //受け取り(step2)
//        lifecycleScope.launch {
//            var i=0
//            Control.ServerReceiveSize()
//            val firstlistsize=Control.ser_res_size
//            Control.ser_res_size?.let { Control.ServerSendNotice(it) }
//            Log.d(TAG, "onCreate: Outside loop Server ${firstlistsize}")
//            while (i<firstlistsize!!){
//                Control.ser_res_size=null
//                Control.ServerReceiveSize()
//                val ser_res_encrypt_second = mutableListOf<ByteArray>()
//                var j=0
//                val secondlistsize = Control.ser_res_size
//                Control.ser_res_size?.let { Control.ServerSendNotice(it) }
//                while (j<secondlistsize!!){
//                    Control.ser_res_size=null
//                    Control.ServerReceiveSize()
//                    val thirdlistsize=Control.ser_res_size
//                    Log.d(TAG, "onCreate: thirdlistsize $thirdlistsize")
//                    Control.ser_res_size?.let { Control.ServerSendNotice(it) }
//                    if (thirdlistsize!=null){
//                        Control.ServerReceive(thirdlistsize)
//                    }
//                    j++
//                    Control.ser_res_mes.let { ser_res_encrypt_second.add(it) }
//                    val res_size=Control.ser_res_mes.size
//                    Control.ServerSendNotice(res_size)
//                }
//                i++
//                ser_res_list_first.add(ser_res_encrypt_second)
//            }
//        }

        Log.d(TAG, "onCreate: Server おしまい")

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

    fun PSISend_first(encmes:List<List<ByteArray>>,viewmodel: ServerViewModel){
        Log.d(TAG, "onCreate: send my encrypt data to client")
        Toast.makeText(this,"通信開始(Server to Client)",Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            Log.d(TAG, "onCreate: Start connect to client")
            Control.ServerConnect()
            Log.d(TAG, "onCreate: Send encrypt message to Client")
            Control.ServerSend(enc_mes_list)
            viewmodel.server_end_flag.value=Control.ser_end_flag
            Log.d(TAG, "PSISend_first: return")
//            Log.d(TAG, "onCreate: Start Receive client encrypt message")
//            Control.ServerReceive()
        }
    }

    fun PSIdoubleencrypt(mes_list_list:List<List<ByteArray>>,pri_key_kt: ByteArray){
        Log.d(TAG, "PSIdoubleencrypt: Start")
        var i=0
        for (mes_list in mes_list_list){
            val enc_result= mutableListOf<ByteArray>()
            for (mes in mes_list){
                val enc_mes : ByteArray= ByteArray(ec_point_length)
                val e=encryptdouble(mes,pri_key_kt,enc_mes)
                enc_result.add(enc_mes)
            }
            enc_mes_double.add(enc_result)
        }
        Log.d(TAG, "PSIdoubleencrypt: return")
    }

    fun PSISendSecond(doubleencmes:List<List<ByteArray>>,viewmodel: ServerViewModel){
        Log.d(TAG, "PSISendSecond: Start")
        lifecycleScope.launch {
            Control.ServerSend(doubleencmes)
            viewmodel.server_end_flag.value=Control.ser_end_flag
        }
        Log.d(TAG, "PSISendSecond: return")
    }



    external fun createKey(key :ByteArray): Boolean

    external fun encryptSet(message: Byte, key: ByteArray, out: ByteArray):Boolean

    external fun encryptdouble(enc_message: ByteArray, key: ByteArray, out: ByteArray):Boolean


    external fun aestest():Boolean


    companion object{
        init {
            System.loadLibrary("kotlinpsi")
        }
    }
}