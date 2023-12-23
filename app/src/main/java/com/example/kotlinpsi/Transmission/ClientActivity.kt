package com.example.kotlinpsi.Transmission

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinpsi.Database.Contact
import com.example.kotlinpsi.Database.ContactApplication
import com.example.kotlinpsi.Database.ContactViewModel
import com.example.kotlinpsi.MainActivity
import com.example.kotlinpsi.R
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ClientActivity : AppCompatActivity() {

    val pri_key_len = 32

    val ec_point_length=65



    val enc_mes_list = mutableListOf<ByteArray>()

    //サーバーに送る共通部分
    lateinit var commonlist_to_server : BooleanArray
    //クライアント用の共通部分
    lateinit var commonlist_for_client : BooleanArray

    //共通要素の場所が入ったリスト
    val commonlist= mutableListOf<Int>()

    private val contactViewModel: ContactViewModel by viewModels {
        ContactViewModel.ContactViewmodelFactory((application as ContactApplication).repository)
    }

    private val flagmodel:ClientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        val ipaddr=intent.getStringExtra(MainActivity.server_ip)
        val radioflag=intent.getIntExtra(MainActivity.radioflag,0)

        val adapter = CommonListAdapter()
        val recycler = findViewById<RecyclerView>(R.id.recycler_client)
        recycler.adapter=adapter
        recycler.layoutManager=LinearLayoutManager(this)

        val pri_key_kt:ByteArray=ByteArray(pri_key_len)
        val f=createkeyClient(pri_key_kt)

        //PSIに使うデータのリスト
        var PSIList : List<Contact> = emptyList()

        //サーバの持つ集合が暗号化されたもの
        val cli_res_encrypt_first= mutableListOf<ByteArray>()
        //サーバから送られる自分の集合
        val double_enc_mes= mutableListOf<ByteArray>()

        //共通集合を表示するためのリスト
        val commonList = mutableListOf<Contact>()

        val clientObserver = Observer<Int>{ flag ->
            //受け取り終えたら行う行程
            //自分が暗号化した履歴をサーバーが暗号化して送ってくるためそれを受け取り，共通部分を計算して送る
            when(flag){
                1 ->{
                    Log.d(TAG, "onCreate: step2 Start")
                    Toast.makeText(this,"start step2",Toast.LENGTH_SHORT).show()
                    //
                    when(radioflag){
                        0 ->{
                            contactViewModel.allLists.observe(this){
                                    contacts -> contacts.let {
                                        Log.d(TAG, "onCreate: PSI step2")
                                        PSIList=contacts
                                        PSIencryptArray(contacts,pri_key_kt)
                                        PSISendClient(enc_mes_list,flagmodel)
                                    }
                            }
                        }
                        1 ->{
                            val now = LocalDateTime.now()
                            val start: LocalDateTime
                            if(now.monthValue==1){
                                start= LocalDateTime.of(now.year-1,12,now.dayOfMonth,now.hour,now.minute)
                            }else{
                                start= LocalDateTime.of(now.year,now.monthValue-1,now.dayOfMonth,now.hour,now.minute)
                            }
                            contactViewModel.SearchMonth(start,now).asLiveData().observe(this){
                                    contacts -> contacts.let {
                                        Log.d(TAG, "onCreate: PSI step2")
                                        PSIList=contacts
                                        PSIencryptArray(contacts,pri_key_kt)
                                        PSISendClient(enc_mes_list,flagmodel)
                                    }
                            }
                        }
                        3->{
                            val now = LocalDateTime.now()
                            val start: LocalDateTime
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
                                        Log.d(TAG, "onCreate: PSI step2")
                                        PSIList=contacts
                                        PSIencryptArray(contacts,pri_key_kt)
                                        PSISendClient(enc_mes_list,flagmodel)
                                    }
                            }
                        }
                    }
                }
                2->{
                    //step3 サーバが暗号化したクライアントの集合を受け取る
                    Log.d(TAG, "onCreate: start step3")
                    Toast.makeText(this,"start step3",Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onCreate: receive list size ${cli_res_encrypt_first.size}")
                    lifecycleScope.launch {
                        Log.d(TAG, "onCreate: step3 start back thread")
                        //
                        var i=0
                        Control.ClientReceiveSize()
                        val firstlistsize=Control.cli_res_size
                        Control.cli_res_size?.let { Control.ClientSendNotice(it) }
                        Log.d(TAG, "onCreate: Outside loop $firstlistsize")
                        while (i<firstlistsize!!){
                            //フラグ初期化
                            Control.cli_res_size=null
                            Control.ClientReceiveSize()
                            val secondlistsize=Control.cli_res_size
                            Control.cli_res_size?.let { Control.ClientSendNotice(it) }
                            if(secondlistsize!=null){
                                Control.ClientReceive(secondlistsize)
                            }
                            Control.cli_res_mes.let { double_enc_mes.add(it) }
                            val res_size = Control.cli_res_mes.size
                            Control.ClientSendNotice(res_size)
                            i++
                        }
                        Control.ClientSendNotice(3)
                        flagmodel.receiveflag.value=3
                    }
                }
                3->{
                    Log.d(TAG, "onCreate: start step4")
                    Toast.makeText(this,"start step4",Toast.LENGTH_SHORT).show()
                    //Log.d(TAG, "onCreate: double encrypt size ${double_enc_mes.size}")
                    //復号と共通部分送信
                    PSIdecryptcalc(double_enc_mes,cli_res_encrypt_first,pri_key_kt)
                    PSISendCommonlist(commonlist_to_server,flagmodel)
                }
                4->{
                    Log.d(TAG, "onCreate: finish")
                    //接続解除
                    Control.DisConnectClient()
                    Toast.makeText(this,"finish",Toast.LENGTH_SHORT).show()
                    //接触していた時間を画面に出力
                    var i=0
                    for(bool in commonlist_for_client){
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

        flagmodel.receiveflag.observe(this,clientObserver)


        lifecycleScope.launch {
            //step1受け取り
            if(ipaddr!=null){
                Control.ClientConnect(ipaddr)
                var i=0
                Control.ClientReceiveSize()
                val firstlistsize=Control.cli_res_size
                Control.cli_res_size?.let { Control.ClientSendNotice(it) }
                Log.d(TAG, "onCreate: Outside loop $firstlistsize")
                while (i<firstlistsize!!){
                    //フラグ初期化
                    Control.cli_res_size=null
                    Control.ClientReceiveSize()
                    val secondlistsize=Control.cli_res_size
                    Control.cli_res_size?.let { Control.ClientSendNotice(it) }
                    if(secondlistsize!=null){
                        Control.ClientReceive(secondlistsize)
                    }
                    Control.cli_res_mes.let { cli_res_encrypt_first.add(it) }
                    val res_size = Control.cli_res_mes.size
                    Control.ClientSendNotice(res_size)
                    i++
                }
                Control.ClientSendNotice(1)
                flagmodel.receiveflag.value=1
            }

        }
    }

    fun PSIencryptArray(contacts: List<Contact>, pri_key_kt: ByteArray){
        Log.d(TAG, "PSIencryptArray: ${contacts}")
        for (mes in contacts){
            val encmes=ByteArray(ec_point_length)
            val e=encryptArrayClient(mes.name,pri_key_kt,encmes)
            enc_mes_list.add(encmes)
        }
    }

    fun PSISendClient(encmes:List<ByteArray>,viewmodel: ClientViewModel){
        Log.d(TAG, "PSISendClient: send data to server")
        //Toast.makeText(this,"通信開始(Client to Server)",Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            Control.ClientSend(encmes)
            viewmodel.receiveflag.value=Control.cli_end_flag
            Log.d(TAG, "PSISendClient: return")
        }
    }

    fun PSIdecryptcalc(doubleencmes:List<ByteArray>,ser_enc:List<ByteArray>,pri_key_kt: ByteArray){
        Log.d(TAG, "PSIdecryptcalc: Start")
        var e=false
        var server_count = 0
        commonlist_to_server= BooleanArray(ser_enc.size)
        commonlist_for_client= BooleanArray(doubleencmes.size)
        for (ser_mes in ser_enc){
            var client_count = 0
            for (double_mes in doubleencmes){
                e=decryptcalc(double_mes,ser_mes,pri_key_kt)
                if(server_count==0){
                    commonlist_for_client[client_count]=e
                }else{
                    if(commonlist_for_client[client_count]==false){
                        commonlist_for_client[client_count]=e
                    }
                }
                if(e){
                    commonlist_to_server[server_count]=e
                }else{
                    if (client_count==0){
                        commonlist_to_server[server_count]=e
                    }
                }
                client_count++
            }
            server_count++
        }
    }

    fun PSISendCommonlist(array: BooleanArray,viewmodel: ClientViewModel){
        lifecycleScope.launch {
            Control.ClientSendCommonlist(array)
            viewmodel.receiveflag.value=Control.cli_end_flag
        }
    }

    external fun createkeyClient(key: ByteArray):Boolean

    external fun decryptcalc(double_mes:ByteArray,ser_mes:ByteArray,key:ByteArray):Boolean

    external fun encryptArrayClient(message: ByteArray,key: ByteArray,out: ByteArray):Boolean

    companion object{
        init {
            System.loadLibrary("kotlinpsi")
        }
    }
}