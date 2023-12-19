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



    val enc_mes_list = mutableListOf<List<ByteArray>>()

    private val contactViewModel: ContactViewModel by viewModels {
        ContactViewModel.ContactViewmodelFactory((application as ContactApplication).repository)
    }

    private val flagmodel:ClientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        val ipaddr=intent.getStringExtra(MainActivity.server_ip)
        val radioflag=intent.getIntExtra(MainActivity.radioflag,0)

        //サーバの持つ集合が暗号化されたもの
        val cli_res_encrypt_first= mutableListOf<List<ByteArray>>()
        var resflag=0

        val mflagObserver = Observer<Int>{ flag ->
            //受け取り終えたら行う行程
            //自分が暗号化した履歴をサーバーが暗号化して送ってくるためそれを受け取り，共通部分を計算して送る
            if(flag==1){
                Log.d(TAG, "onCreate: Observer Start")

                //
                when(radioflag){
                    0 ->{
                        contactViewModel.allLists.observe(this){
                                contacts -> contacts.let {
                            Log.d(TAG, "onCreate: ${contacts.toString()}")
                            Toast.makeText(this,"PSI開始",Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "onCreate: PSIStart")
                            Log.d(TAG, "onCreate: PSI step2")

                            val pri_key_kt:ByteArray=ByteArray(pri_key_len)
                            val f=createkeyClient(pri_key_kt)
                            var i=1 //テスト用変数
                            PSIencryptClient(contacts,pri_key_kt)
                            PSISendClient(enc_mes_list)
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
                            Log.d(TAG, "onCreate: ${contacts.toString()}")
                            Toast.makeText(this,"PSI開始",Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "onCreate: PSIStart")
                            Log.d(TAG, "onCreate: PSI step2")

                            val pri_key_kt:ByteArray=ByteArray(pri_key_len)
                            val f=createkeyClient(pri_key_kt)
                            var i=1 //テスト用変数
                            PSIencryptClient(contacts,pri_key_kt)
                            PSISendClient(enc_mes_list)
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
                            Log.d(TAG, "onCreate: ${contacts.toString()}")
                            Toast.makeText(this,"PSI開始",Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "onCreate: PSIStart")
                            Log.d(TAG, "onCreate: PSI step2")

                            val pri_key_kt:ByteArray=ByteArray(pri_key_len)
                            val f=createkeyClient(pri_key_kt)
                            var i=1 //テスト用変数
                            PSIencryptClient(contacts,pri_key_kt)
                            PSISendClient(enc_mes_list)
                        }
                        }
                    }
                }

                //
            }
        }

        flagmodel.receiveflag.observe(this,mflagObserver)







        lifecycleScope.launch {
            if (ipaddr!=null){
                Control.ClientConnect(ipaddr)
                var i_cli=0
                Control.ClientReceiveSize()
                val firstlistsize=Control.cli_res_size
                Control.cli_res_size?.let { Control.ClientSendNotice(it) }
                Log.d(TAG, "onCreate: Outside loop $firstlistsize")
                while(i_cli<firstlistsize!!){
                    //フラグを初期化
                    Control.cli_res_size=null
                    Control.ClientReceiveSize()
                    val cli_res_encrypt_second= mutableListOf<ByteArray>()
                    var j_cli=0
                    val secondlistsize=Control.cli_res_size
                    Control.cli_res_size?.let { Control.ClientSendNotice(it) }
                    while (j_cli<secondlistsize!!){
                        //フラグの初期化
                        Control.cli_res_size=null
                        Control.ClientReceiveSize()
                        val thirdlistsize=Control.cli_res_size
                        Log.d(TAG, "onCreate: thirdlistsize $thirdlistsize")
                        Control.cli_res_size?.let { Control.ClientSendNotice(it) }
                        if (thirdlistsize!=null){
                            Control.ClientReceive(thirdlistsize)
                        }
                        j_cli++
                        Control.cli_res_mes.let { cli_res_encrypt_second.add(it) }
                        val res_size=Control.cli_res_mes.size
                        Control.ClientSendNotice(res_size)
                    }
                    i_cli++
                    cli_res_encrypt_first.add(cli_res_encrypt_second)
                }
                resflag=1
                flagmodel.receiveflag.value=1
            }
        }

        Log.d(TAG, "onCreate: Client おしまい")
    }

    fun PSIencryptClient(contacts: List<Contact>,pri_key_kt:ByteArray){
        Log.d(TAG, "PSIencryptClient: Start")
        for (message in contacts){
            val enc_result= mutableListOf<ByteArray>()
            for (name in message.name){
                val enc_mes:ByteArray=ByteArray(ec_point_length)
                val e=encryptClient(name,pri_key_kt,enc_mes)
                enc_result.add(enc_mes)
                if(e){
                    Log.d(TAG, "PSIencryptClient: OK")
                }
            }
            enc_mes_list.add(enc_result)
        }
        Log.d(TAG, "onCreate: PSI finish step1 No.${contacts.size}")
    }

    fun PSISendClient(encmes:List<List<ByteArray>>){
        Log.d(TAG, "PSISendClient: send my encrypt data to server")
        Toast.makeText(this,"通信開始(Client to Server)",Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            Log.d(TAG, "PSISendClient: Start connect to server")
            Control.ClientSend(enc_mes_list)
        }
    }

    external fun createkeyClient(key: ByteArray):Boolean

    external fun encryptClient(message : Byte , key: ByteArray, out: ByteArray):Boolean

    companion object{
        init {
            System.loadLibrary("kotlinpsi")
        }
    }
}