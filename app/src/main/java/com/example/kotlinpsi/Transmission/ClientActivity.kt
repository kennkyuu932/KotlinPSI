package com.example.kotlinpsi.Transmission

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.example.kotlinpsi.Database.ContactApplication
import com.example.kotlinpsi.Database.ContactViewModel
import com.example.kotlinpsi.MainActivity
import com.example.kotlinpsi.R
import kotlinx.coroutines.launch

class ClientActivity : AppCompatActivity() {

    val pri_key_len = 32

    val ec_point_length=65

    val enc_mes_list = mutableListOf<List<ByteArray>>()

    private val contactViewModel: ContactViewModel by viewModels {
        ContactViewModel.ContactViewmodelFactory((application as ContactApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        val ipaddr=intent.getStringExtra(MainActivity.server_ip)
        val radioflag=intent.getIntExtra(MainActivity.radioflag,0)

        //サーバの持つ集合が暗号化されたもの
        val cli_res_encrypt_first= mutableListOf<List<ByteArray>>()


        contactViewModel.allLists.observe(owner = this){
            contacts -> contacts.let {
                Log.d(TAG, "onCreate: ${contacts.toString()}")
                Toast.makeText(this,"PSI開始",Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onCreate: PSIStart")
                Log.d(TAG, "onCreate: PSI step2")

                val pri_key_kt:ByteArray=ByteArray(pri_key_len)
                val f=createkeyClient(pri_key_kt)
                var i=1 //テスト用変数
            }
        }



        lifecycleScope.launch {
            if (ipaddr!=null){
                Control.ClientConnect(ipaddr)
                var i=0
                Control.ClientReceiveSize()
                val firstlistsize=Control.cli_res_size
                Control.cli_res_size?.let { Control.ClientSendNotice(it) }
                Log.d(TAG, "onCreate: Outside loop $firstlistsize")
                while(i<firstlistsize!!){
                    //フラグを初期化
                    Control.cli_res_size=null
                    Control.ClientReceiveSize()
                    val cli_res_encrypt_second= mutableListOf<ByteArray>()
                    var j=0
                    val secondlistsize=Control.cli_res_size
                    Control.cli_res_size?.let { Control.ClientSendNotice(it) }
                    while (j<secondlistsize!!){
                        //フラグの初期化
                        Control.cli_res_size=null
                        Control.ClientReceiveSize()
                        val thirdlistsize=Control.cli_res_size
                        Log.d(TAG, "onCreate: thirdlistsize $thirdlistsize")
                        Control.cli_res_size?.let { Control.ClientSendNotice(it) }
                        if (thirdlistsize!=null){
                            Control.ClientReceive(thirdlistsize)
                        }
                        j++
                        Control.cli_res_mes.let { cli_res_encrypt_second.add(it) }
                        val res_size=Control.cli_res_mes.size
                        Control.ClientSendNotice(res_size)
                    }
                    i++
                    cli_res_encrypt_first.add(cli_res_encrypt_second)
                }
            }
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