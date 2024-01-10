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
import java.sql.Timestamp
import java.time.LocalDateTime

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
                    MainActivity.receive_start_first=kotlin.system.measureNanoTime {
                        lifecycleScope.launch {
                            var i=0
                            Control.ServerReceiveSize()
                            val firstlistsize=Control.ser_res_size
                            Control.ser_res_size?.let { Control.ServerSendNotice(it) }
                            //Log.d(TAG, "onCreate: Otside loop $firstlistsize")
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
                }
                2->{
                    //step3 クライアントからもらった集合を暗号化して送る．
                    Toast.makeText(this,"start step3",Toast.LENGTH_SHORT).show()
                    MainActivity.encrypt_start_second=kotlin.system.measureNanoTime {
                        PSIdubleencrypt(ser_res_list_first,pri_key_kt)
                    }
                    //PSIdubleencrypt(ser_res_list_first,pri_key_kt)
                    MainActivity.send_start_second=kotlin.system.measureNanoTime {
                        PSISendSecond(enc_mes_double,serverviewmodel)
                    }
                    //PSISendSecond(enc_mes_double,serverviewmodel)
                }
                3->{
                    //step4 クライアントから共通部分の場所をもらう
                    Toast.makeText(this,"start step4",Toast.LENGTH_SHORT).show()
                    //Log.d(TAG, "onCreate: start step4")
                    MainActivity.receive_start_second=kotlin.system.measureNanoTime {
                        lifecycleScope.launch {
                            var i=0
                            Control.ServerReceiveSize()
                            val firstlistsize = Control.ser_res_size
                            Control.ser_res_size?.let { Control.ServerSendNotice(it) }
                            //Log.d(TAG, "onCreate: Outside loop $firstlistsize")
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
                }
                4->{
                    //Log.d(TAG, "onCreate: finish")
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
                    if(commonList.isEmpty()){
                        val empty = findViewById<TextView>(R.id.common_text)
                        empty.setText("共通集合はありません")
                    }else{
                        adapter.submitList(commonList)
                    }
                    //時間をログに表示する(ナノ秒)
                    Log.d(MainActivity.TAG_TIME, "接触履歴の暗号化にかかった時間(ナノ秒) : ${MainActivity.encrypt_start_first}")
                    Log.d(MainActivity.TAG_TIME,"暗号化した自分の接触履歴を送るのにかかった時間(ナノ秒) : ${MainActivity.send_start_first}")
                    Log.d(MainActivity.TAG_TIME,"暗号化された相手の接触履歴を受け取るのにかかった時間(ナノ秒) : ${MainActivity.receive_start_first}")
                    Log.d(MainActivity.TAG_TIME,"暗号化されら接触履歴を暗号化するのにかかった時間(ナノ秒) : ${MainActivity.encrypt_start_second}")
                    Log.d(MainActivity.TAG_TIME,"再暗号化した接触履歴を送るのにかかった時間(ナノ秒) : ${MainActivity.send_start_second}")
                    Log.d(MainActivity.TAG_TIME,"共通集合を受け取るのにかかった時間(ナノ秒) : ${MainActivity.receive_start_second}")
                }
            }


        }

        serverviewmodel.server_end_flag.observe(this,mserendflagObserver)


        when (radioflag) {
            0 -> {
                //Log.d(TAG, "onCreate: 全部のデータでPSI")
                contactViewModel.allLists.observe(this) { contacts ->
                    contacts.let {
                        PSIList=contacts
                        //Log.d(TAG, "onCreate: $contacts")
                        //Log.d(MainActivity.TAG_TIME, "onCreate: Start encrypt time ${LocalDateTime.now().minute}:${LocalDateTime.now().second}:${LocalDateTime.now().nano}")
                        MainActivity.encrypt_start_first=kotlin.system.measureNanoTime {
                            PSIencryptArray(contacts,pri_key_kt)
                        }
                        //PSIencryptArray(contacts,pri_key_kt)
                        //Log.d(MainActivity.TAG_TIME, "onCreate: finish encrypt time ${LocalDateTime.now().minute}:${LocalDateTime.now().second}:${LocalDateTime.now().nano}")
                        MainActivity.send_start_first=kotlin.system.measureNanoTime {
                            PSISendfirst(enc_mes_list,serverviewmodel)
                        }
                        //PSISendfirst(enc_mes_list,serverviewmodel)
                    }
                }
            }
            1->{
                //Log.d(TAG, "onCreate: 現時点から1ヶ月分のデータでPSI")
                val now = LocalDateTime.now()
                val start:LocalDateTime
                if(now.monthValue==1){
                    start= LocalDateTime.of(now.year-1,12,now.dayOfMonth,now.hour,now.minute)
                }else{
                    start=LocalDateTime.of(now.year,now.monthValue-1,now.dayOfMonth,now.hour,now.minute)
                }
                contactViewModel.SearchMonth(start,now).asLiveData().observe(owner = this){
                    contacts -> contacts.let {
                    //Log.d(TAG, "onCreate: $contacts")
                        PSIList=contacts
                        MainActivity.encrypt_start_first=kotlin.system.measureNanoTime {
                            PSIencryptArray(contacts,pri_key_kt)
                        }
                        MainActivity.send_start_first=kotlin.system.measureNanoTime {
                            PSISendfirst(enc_mes_list,serverviewmodel)
                        }
                        //PSIencryptArray(contacts,pri_key_kt)
                        //PSISendfirst(enc_mes_list,serverviewmodel)
                    }
                }
            }
            3->{
                //Log.d(TAG, "onCreate: 現時点から3ヶ月分のデータでPSI")
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
                    //Log.d(TAG, "onCreate: $contacts")
                            MainActivity.encrypt_start_first=kotlin.system.measureNanoTime {
                                PSIencryptArray(contacts,pri_key_kt)
                            }
                            MainActivity.send_start_first=kotlin.system.measureNanoTime {
                                PSISendfirst(enc_mes_list,serverviewmodel)
                            }
                            //PSIencryptArray(contacts,pri_key_kt)
                            //PSISendfirst(enc_mes_list,serverviewmodel)
                        }
                }
            }
        }

    }


    fun PSIencryptArray(contacts: List<Contact>, pri_key_kt: ByteArray){
        //Log.d(TAG, "PSIencryptArray: ${contacts}")
        for (mes in contacts){
            val enc_mes = ByteArray(ec_point_length)
            val e=encryptSetArray(mes.name,pri_key_kt,enc_mes)
            enc_mes_list.add(enc_mes)
        }
        //Log.d(TAG, "PSIencryptArray: PSI finish step1 No.${contacts.size}")
    }

    fun PSISendfirst(encmes:List<ByteArray>,viewmodel: ServerViewModel){
        //Log.d(TAG, "PSISend_first: send my encrypt data to client")
        Toast.makeText(this,"通信開始(Server to Client)",Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            //Log.d(TAG, "PSISend_first: Start connect to client")
            Control.ServerConnect()
            //Log.d(TAG, "PSISend_first: Send encrypt message to Client")
            Control.ServerSend(encmes)
            viewmodel.server_end_flag.value=Control.ser_end_flag
            //Log.d(TAG, "PSISend_first: return")
        }
    }

    fun PSIdubleencrypt(mes_list:List<ByteArray>,pri_key_kt: ByteArray){
        //Log.d(TAG, "PSIdubleencrypt: Start")
        for (mes in mes_list){
            val encmes=ByteArray(ec_point_length)
            val e=encryptdouble(mes,pri_key_kt,encmes)
            enc_mes_double.add(encmes)
        }
        //Log.d(TAG, "PSIdubleencrypt: return")
    }

    fun PSISendSecond(doubleencmes: List<ByteArray>,viewmodel: ServerViewModel){
        //Log.d(TAG, "PSISendSecond: Start")
        lifecycleScope.launch {
            Control.ServerSend(doubleencmes)
            viewmodel.server_end_flag.value=Control.ser_end_flag
        }
        //Log.d(TAG, "PSISendSecond: return")
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