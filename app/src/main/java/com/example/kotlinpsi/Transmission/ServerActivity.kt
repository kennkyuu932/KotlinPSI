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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinpsi.Database.Contact
import com.example.kotlinpsi.MainActivity
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.log

class ServerActivity : AppCompatActivity() {

    val pri_key_len=32
    val ec_point_length=65

    //自分の集合を暗号化したもの
    val enc_mes_list= mutableListOf<ByteArray>()

    //クライアントから受け取った集合を暗号化したもの
    val enc_mes_double = mutableListOf<ByteArray>()

    private val contactViewModel:ContactViewModel by viewModels {
        ContactViewModel.ContactViewmodelFactory((application as ContactApplication).repository)
    }

    private val serverviewmodel:ServerViewModel by viewModels()

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

        val adapter=CommonListAdapter()
        val recycler = findViewById<RecyclerView>(R.id.recycler_server)
        recycler.adapter=adapter
        recycler.layoutManager=LinearLayoutManager(this)

        val radioflag=intent.getIntExtra(MainActivity.radioflag,0)

        val pri_key_kt:ByteArray = ByteArray(pri_key_len)
        val f=createKey(pri_key_kt)

        //PSIに使うデータのリスト
        var PSIList : List<Contact> = emptyList()

        //クライアントから受け取った暗号データ
        val ser_res_list_first= mutableListOf<ByteArray>()

        //クライアントから送られてくる共通部分が入ったデータ
        val ser_res_common = mutableListOf<Boolean>()
        //サーバーが共通部分を表示するためのリスト
        val commonList = mutableListOf<Contact>()

        val mserendflagObserver = Observer<Int>{ flag ->
            when(flag){
                1->{
                    Toast.makeText(this,"finish step1",Toast.LENGTH_SHORT).show()
                    //受け取り(step2)
                    lifecycleScope.launch {
                        var i=0
                        Control.ServerReceiveSize()
                        val firstlistsize=Control.ser_res_size
                        Control.ser_res_size?.let { Control.ServerSendNotice(it) }
                        Log.d(TAG, "onCreate: Otside loop $firstlistsize")
                        while (i<firstlistsize!!){
                            //フラグ初期化
                            Control.ser_res_size=null
                            Control.ServerReceiveSize()
                            val secondlistsize=Control.ser_res_size
                            Control.ser_res_size?.let { Control.ServerSendNotice(it) }
                            if(secondlistsize!=null){
                                Control.ServerReceive(secondlistsize)
                            }
                            Control.ser_res_mes.let { ser_res_list_first.add(it) }
                            val res_size=Control.ser_res_mes.size
                            Control.ServerSendNotice(res_size)
                            i++
                        }
                        Control.ServerSendNotice(2)
                        serverviewmodel.server_end_flag.value=2
                    }
                }
                2->{
                    //step3 クライアントからもらった集合を暗号化して送る．
                    Toast.makeText(this,"start step3",Toast.LENGTH_SHORT).show()
                    PSIdubleencrypt(ser_res_list_first,pri_key_kt)
                    PSISendSecond(enc_mes_double,serverviewmodel)
                }
                3->{
                    //step4 クライアントから共通部分の場所をもらう
                    Toast.makeText(this,"start step4",Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onCreate: start step4")
                    lifecycleScope.launch {
                        var i=0
                        Control.ServerReceiveSize()
                        val firstlistsize = Control.ser_res_size
                        Control.ser_res_size?.let { Control.ServerSendNotice(it) }
                        Log.d(TAG, "onCreate: Outside loop $firstlistsize")
                        while (i<firstlistsize!!){
                            Control.ser_res_size=null
                            Control.ServerReceiveCommonList()
                            Control.ser_res_common?.let { ser_res_common.add(it) }
                            Control.ServerSendNotice(1)
                            i++
                        }
                        Control.ServerSendNotice(4)
                        serverviewmodel.server_end_flag.value=4
                    }
                }
                4->{
                    Log.d(TAG, "onCreate: finish")
                    Toast.makeText(this,"finish",Toast.LENGTH_SHORT).show()
                    //接続解除
                    Control.DisConnectServer()
                    //接触時間の表示
                    var i=0
                    for(bool in ser_res_common){
                        if (bool){
                            if(commonList.size!=0){
                                   if(PSIList[i-1].date!=PSIList[i].date){
                                       commonList.add(PSIList[i])
                                   }
                            }else{
                                commonList.add(PSIList[i])
                            }
                        }
                        i++
                    }
                    adapter.submitList(commonList)
                }
            }


        }

        serverviewmodel.server_end_flag.observe(this,mserendflagObserver)


        when (radioflag) {
            0 -> {
                Log.d(TAG, "onCreate: 全部のデータでPSI")
                contactViewModel.allLists.observe(this) { contacts ->
                    contacts.let {
                        PSIList=contacts
                        PSIencryptArray(contacts,pri_key_kt)
                        PSISendfirst(enc_mes_list,serverviewmodel)
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
                        PSIList=contacts
                        PSIencryptArray(contacts,pri_key_kt)
                        PSISendfirst(enc_mes_list,serverviewmodel)
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
                            PSIList=contacts
                            PSIencryptArray(contacts,pri_key_kt)
                            PSISendfirst(enc_mes_list,serverviewmodel)
                        }
                }
            }
        }

    }


    fun PSIencryptArray(contacts: List<Contact>, pri_key_kt: ByteArray){
        Log.d(TAG, "PSIencryptArray: ${contacts}")
        for (mes in contacts){
            val enc_mes = ByteArray(ec_point_length)
            val e=encryptSetArray(mes.name,pri_key_kt,enc_mes)
            enc_mes_list.add(enc_mes)
        }

        Log.d(TAG, "PSIencryptArray: PSI finish step1 No.${contacts.size}")
    }

    fun PSISendfirst(encmes:List<ByteArray>,viewmodel: ServerViewModel){
        Log.d(TAG, "PSISend_first: send my encrypt data to client")
        Toast.makeText(this,"通信開始(Server to Client)",Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            Log.d(TAG, "PSISend_first: Start connect to client")
            Control.ServerConnect()
            Log.d(TAG, "PSISend_first: Send encrypt message to Client")
            Control.ServerSend(encmes)
            viewmodel.server_end_flag.value=Control.ser_end_flag
            Log.d(TAG, "PSISend_first: return")
        }
    }

    fun PSIdubleencrypt(mes_list:List<ByteArray>,pri_key_kt: ByteArray){
        Log.d(TAG, "PSIdubleencrypt: Start")
        for (mes in mes_list){
            val encmes=ByteArray(ec_point_length)
            val e=encryptdouble(mes,pri_key_kt,encmes)
            enc_mes_double.add(encmes)
        }
        Log.d(TAG, "PSIdubleencrypt: return")
    }

    fun PSISendSecond(doubleencmes: List<ByteArray>,viewmodel: ServerViewModel){
        Log.d(TAG, "PSISendSecond: Start")
        lifecycleScope.launch {
            Control.ServerSend(doubleencmes)
            viewmodel.server_end_flag.value=Control.ser_end_flag
        }
        Log.d(TAG, "PSISendSecond: return")
    }



    external fun createKey(key :ByteArray): Boolean

    external fun encryptSetArray(message:ByteArray,key:ByteArray,out: ByteArray):Boolean

    external fun encryptdouble(enc_message: ByteArray, key: ByteArray, out: ByteArray):Boolean


    companion object{
        init {
            System.loadLibrary("kotlinpsi")
        }
    }
}