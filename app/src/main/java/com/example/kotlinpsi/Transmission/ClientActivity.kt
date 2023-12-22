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
    val enc_mes_list2 = mutableListOf<ByteArray>()

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

        val pri_key_kt:ByteArray=ByteArray(pri_key_len)
        val f=createkeyClient(pri_key_kt)

        //サーバの持つ集合が暗号化されたもの
        val cli_res_encrypt_first= mutableListOf<List<ByteArray>>()
        val cli_res_encrypt_first2= mutableListOf<ByteArray>()
        //サーバから送られる自分の集合
        val double_enc_mes= mutableListOf<List<ByteArray>>()
        val double_enc_mes2= mutableListOf<ByteArray>()

        var resflag=0

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
                                        Log.d(TAG, "onCreate: ${contacts.toString()}")
                                        //Toast.makeText(this,"PSI開始",Toast.LENGTH_SHORT).show()
                                        Log.d(TAG, "onCreate: PSIStart")
                                        Log.d(TAG, "onCreate: PSI step2")

                                        var i=1 //テスト用変数
                                        PSIencryptClient(contacts,pri_key_kt)
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
                                        Log.d(TAG, "onCreate: ${contacts.toString()}")
                                        //Toast.makeText(this,"PSI開始",Toast.LENGTH_SHORT).show()
                                        Log.d(TAG, "onCreate: PSIStart")
                                        Log.d(TAG, "onCreate: PSI step2")

                                        var i=1 //テスト用変数
//                                        PSIencryptClient(contacts,pri_key_kt)
//                                        PSISendClient(enc_mes_list,flagmodel)
                                        PSIencryptArray(contacts,pri_key_kt)
                                        PSISendClient2(enc_mes_list2,flagmodel)
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
                                        //Toast.makeText(this,"PSI開始",Toast.LENGTH_SHORT).show()
                                        Log.d(TAG, "onCreate: PSIStart")
                                        Log.d(TAG, "onCreate: PSI step2")

                                        var i=1 //テスト用変数
                                        PSIencryptClient(contacts,pri_key_kt)
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
                            Control.cli_res_mes.let { double_enc_mes2.add(it) }
                            val res_size = Control.cli_res_mes.size
                            Control.ClientSendNotice(res_size)
                            i++
                        }
                        resflag=1
                        Control.ClientSendNotice(3)
                        flagmodel.receiveflag.value=3

                        //
//                        var i=0
//                        Control.cli_res_size=null
//                        Control.ClientReceiveSize()
//                        val firstlistsize=Control.cli_res_size
//                        Control.cli_res_size?.let { Control.ClientSendNotice(it) }
//                        Log.d(TAG, "onCreate: Outside loop $firstlistsize")
//                        while(i<firstlistsize!!){
//                            //フラグを初期化
//                            Control.cli_res_size=null
//                            Control.ClientReceiveSize()
//                            val cli_res_encrypt_second= mutableListOf<ByteArray>()
//                            var j=0
//                            val secondlistsize=Control.cli_res_size
//                            Control.cli_res_size?.let { Control.ClientSendNotice(it) }
//                            while (j<secondlistsize!!){
//                                //フラグの初期化
//                                Control.cli_res_size=null
//                                Control.ClientReceiveSize()
//                                val thirdlistsize=Control.cli_res_size
//                                Log.d(TAG, "onCreate: thirdlistsize $thirdlistsize")
//                                Control.cli_res_size?.let { Control.ClientSendNotice(it) }
//                                if (thirdlistsize!=null){
//                                    Control.ClientReceive(thirdlistsize)
//                                }
//                                j++
//                                Control.cli_res_mes.let { cli_res_encrypt_second.add(it) }
//                                val res_size=Control.cli_res_mes.size
//                                Control.ClientSendNotice(res_size)
//                            }
//                            i++
//                            double_enc_mes.add(cli_res_encrypt_second)
//                        }
//                        resflag=1
//                        Control.ClientSendNotice(3)
//                        flagmodel.receiveflag.value=3
                    }
                }
                3->{
                    Log.d(TAG, "onCreate: start step4")
                    Toast.makeText(this,"start step4",Toast.LENGTH_SHORT).show()
                    //Log.d(TAG, "onCreate: double encrypt size ${double_enc_mes.size}")
                    //復号と共通部分送信
                    Log.d(TAG, "onCreate: start step4 double enc size ${double_enc_mes2.size}")
                    //PSIdecryptcalc(double_enc_mes,cli_res_encrypt_first,pri_key_kt)
                    PSIdecryptcalc2(double_enc_mes2,cli_res_encrypt_first2,pri_key_kt)
                }


                //
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
                    Control.cli_res_mes.let { cli_res_encrypt_first2.add(it) }
                    val res_size = Control.cli_res_mes.size
                    Control.ClientSendNotice(res_size)
                    i++
                }
                resflag=1
                Control.ClientSendNotice(1)
                flagmodel.receiveflag.value=1
            }



//            if (ipaddr!=null){
//                Control.ClientConnect(ipaddr)
//                var i=0
//                Control.ClientReceiveSize()
//                val firstlistsize=Control.cli_res_size
//                Control.cli_res_size?.let { Control.ClientSendNotice(it) }
//                Log.d(TAG, "onCreate: Outside loop $firstlistsize")
//                while(i<firstlistsize!!){
//                    //フラグを初期化
//                    Control.cli_res_size=null
//                    Control.ClientReceiveSize()
//                    val cli_res_encrypt_second= mutableListOf<ByteArray>()
//                    var j=0
//                    val secondlistsize=Control.cli_res_size
//                    Control.cli_res_size?.let { Control.ClientSendNotice(it) }
//                    while (j<secondlistsize!!){
//                        //フラグの初期化
//                        Control.cli_res_size=null
//                        Control.ClientReceiveSize()
//                        val thirdlistsize=Control.cli_res_size
//                        Log.d(TAG, "onCreate: thirdlistsize $thirdlistsize")
//                        Control.cli_res_size?.let { Control.ClientSendNotice(it) }
//                        if (thirdlistsize!=null){
//                            Control.ClientReceive(thirdlistsize)
//                        }
//                        j++
//                        Control.cli_res_mes.let { cli_res_encrypt_second.add(it) }
//                        val res_size=Control.cli_res_mes.size
//                        Control.ClientSendNotice(res_size)
//                    }
//                    i++
//                    cli_res_encrypt_first.add(cli_res_encrypt_second)
//                }
//                resflag=1
//                Control.ClientSendNotice(1)
//                flagmodel.receiveflag.value=1
//            }
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

    fun PSISendClient(encmes:List<List<ByteArray>>,viewmodel: ClientViewModel){
        Log.d(TAG, "PSISendClient: send my encrypt data to server")
        //Toast.makeText(this,"通信開始(Client to Server)",Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            Log.d(TAG, "PSISendClient: Start connect to server")
            Control.ClientSend(enc_mes_list)
            viewmodel.receiveflag.value=Control.cli_end_flag
            Log.d(TAG, "PSISendClient: return")
        }
    }

    fun PSIdecryptcalc(doubleenc:List<List<ByteArray>>,ser_enc_list:List<List<ByteArray>>,pri_key_kt: ByteArray){
        Log.d(TAG, "PSIdecryptcalc: Start")
//        for(ser_list in ser_enc_list){
//            for(mes_list in doubleenc){
//                var j=0
//                //val testlist2= mutableListOf<List<Int>>()
//                val testlist2= mutableListOf<BooleanArray>()
//                for(ser_enc_mes in ser_list){
//                    var e=true
//                    var i=0
//                    //val testlist= mutableListOf<Int>()
//                    val testlist = BooleanArray(ser_list.size)
//                    for (mes in mes_list){
//                        e=decryptcalc(mes,ser_enc_mes,pri_key_kt)
//                        testlist[i]=e
////                        if(e){
////                            //testlist.add(i)
////                            testlist[i]=true
////                        }
//                        i++
//                    }
//                    //testlist2.add(testlist)
//                    testlist2.add(testlist)
//                }
//                //一致箇所のチェック
//            }
//        }

//        for (ser_list in ser_enc_list){
//            var i=0
//            for (ser_enc_mes in ser_list){
//                for (mes_list in doubleenc){
//                    var flag=true
//                    for (mes in mes_list){
//                        val e=decryptcalc(mes,ser_enc_mes,pri_key_kt)
//                        if(!e){
//                            flag=false
//                            break
//                        }
//                    }
//                    Log.d(TAG, "PSIdecryptcalc: test")
//                    if(flag){
//                        commonlist.add(i)
//                        i++
//                    }
//                }
//            }
//        }
//        for (test in commonlist){
//            Log.d(TAG, "PSIdecryptcalc: $test")
//        }
        Log.d(TAG, "PSIdecryptcalc: return")
    }

    fun PSIencryptArray(contacts: List<Contact>, pri_key_kt: ByteArray){
        Log.d(TAG, "PSIencryptArray: ${contacts}")
        for (mes in contacts){
            val encmes=ByteArray(ec_point_length)
            val e=encryptArrayClient(mes.name,pri_key_kt,encmes)
            enc_mes_list2.add(encmes)
        }
    }

    fun PSISendClient2(encmes:List<ByteArray>,viewmodel: ClientViewModel){
        Log.d(TAG, "PSISendClient2: send data to server")
        //Toast.makeText(this,"通信開始(Client to Server)",Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            Control.ClientSend2(encmes)
            viewmodel.receiveflag.value=Control.cli_end_flag
            Log.d(TAG, "PSISendClient2: return")
        }
    }

    fun PSIdecryptcalc2(doubleencmes:List<ByteArray>,ser_enc:List<ByteArray>,pri_key_kt: ByteArray){
        Log.d(TAG, "PSIdecryptcalc2: Start")
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
        for (test in commonlist_for_client){
            Log.d(TAG, "PSIdecryptcalc2: commonlist for client : ${test.toString()}")
        }
        for(test in commonlist_to_server){
            Log.d(TAG, "PSIdecryptcalc2: commonlist to server : ${test.toString()}")
        }
    }

    external fun createkeyClient(key: ByteArray):Boolean

    external fun encryptClient(message : Byte , key: ByteArray, out: ByteArray):Boolean

    external fun decryptcalc(double_mes:ByteArray,ser_mes:ByteArray,key:ByteArray):Boolean

    external fun encryptArrayClient(message: ByteArray,key: ByteArray,out: ByteArray):Boolean

    companion object{
        init {
            System.loadLibrary("kotlinpsi")
        }
    }
}