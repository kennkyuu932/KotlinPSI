package com.example.kotlinpsi.Transmission

import android.content.ContentValues.TAG
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.math.log

object Control {

    var ser_res_mes:String?=null
    var cli_res_mes:String?=null
    private var ser_serversoc:ServerSocket?=null
    private var ser_socket:Socket?=null
    private var ser_Dis:DataInputStream?=null
    private var ser_Dos:DataOutputStream?=null
    private var cli_socket:Socket?=null
    private var cli_Dis:DataInputStream?=null
    private var cli_Dos:DataOutputStream?=null

    const val PORT: Int = 50000

    suspend fun ServerConnect()= withContext(Dispatchers.IO){
        Log.d(TAG, "ServerConnect: Start")

        try {
            ser_serversoc= ServerSocket(PORT)
            ser_serversoc?.reuseAddress=true
            Log.d(TAG, "ServerConnect: try")
//            while (true){
//                Log.d(TAG, "ServerConnect: while1")
//                ser_socket= ser_serversoc?.accept()
//                Log.d(TAG, "ServerConnect: while")
//            }
            ser_socket= ser_serversoc?.accept()
            Log.d(TAG, "ServerConnect: finish")
        }catch (_:Exception){}
        Log.d(TAG, "ServerConnect: return")
    }

    suspend fun ServerSendMessage(send_mes:String)= withContext(Dispatchers.IO){
        Log.d(TAG, "ServerSendMessage: Start")
        try {
            ser_Dos= DataOutputStream(BufferedOutputStream(ser_socket?.getOutputStream()))
            ser_Dos?.writeUTF(send_mes)
            ser_Dos?.flush()
        }catch (_:Exception){}
        Log.d(TAG, "ServerSendMessage: return")
    }

    suspend fun ServerReceiveMessage()= withContext(Dispatchers.IO){
        Log.d(TAG, "ServerReceiveMessage: Start")
        try {
            ser_Dis= DataInputStream(BufferedInputStream(ser_socket?.getInputStream()))
            ser_res_mes= ser_Dis?.readUTF()
        }catch (_:Exception){}
        Log.d(TAG, "ServerReceiveMessage: return")
    }

    suspend fun ClientConnect(ipaddr:String)= withContext(Dispatchers.IO){
        Log.d(TAG, "ClientConnect: Start")
        try {
            if (cli_socket==null){
                cli_socket=Socket(ipaddr, PORT)
                Log.d(TAG, "ClientConnect: finish")
            }
        }catch (_:Exception){}
        Log.d(TAG, "ClientConnect: return")
    }

    suspend fun ClientSendMessage(send_mes: String)= withContext(Dispatchers.IO){
        Log.d(TAG, "ClientSendMessage: start")
        try {
            cli_Dos= DataOutputStream(BufferedOutputStream(cli_socket?.getOutputStream()))
            cli_Dos?.writeUTF(send_mes)
            cli_Dos?.flush()
        }catch (_:Exception){}
        Log.d(TAG, "ClientSendMessage: return")
    }

    suspend fun ClientReceiveMessage()= withContext(Dispatchers.IO){
        Log.d(TAG, "ClientReceiveMessage: Start")
        try {
            cli_Dis= DataInputStream(BufferedInputStream(cli_socket?.getInputStream()))
            cli_res_mes= cli_Dis?.readUTF()
        }catch (_:Exception){}
        Log.d(TAG, "ClientReceiveMessage: return")
    }

    fun ServerDisConnect(){
        Log.d(TAG, "ServerDisConnect: Start")
        ser_serversoc?.close()
        ser_socket?.close()
        ser_Dis?.close()
        ser_Dos?.close()
        cli_socket?.close()
        cli_Dis?.close()
        cli_Dos?.close()
        Log.d(TAG, "ServerDisConnect: return")
    }
}