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


object Control {

    var ser_res_size:Int?=null
    var cli_res_size:Int?=null
    var ser_end_flag:Int?=null
    var cli_end_flag:Int?=null
    var ser_res_common:Boolean?=null
    lateinit var ser_res_mes:ByteArray
    lateinit var cli_res_mes:ByteArray
    private var ser_serversoc: ServerSocket?=null
    private var ser_socket: Socket?=null
    private var ser_Dis: DataInputStream?=null
    private var ser_Dos: DataOutputStream?=null
    private var cli_socket: Socket?=null
    private var cli_Dis: DataInputStream?=null
    private var cli_Dos: DataOutputStream?=null

    const val PORT: Int = 50000


    suspend fun ServerConnect()= withContext(Dispatchers.IO){
        //Log.d(TAG, "ServerConnect: Start")
        try {
            ser_serversoc= ServerSocket(PORT)
            ser_serversoc?.reuseAddress=true
            //Log.d(TAG, "ServerConnect: try")
            ser_socket= ser_serversoc?.accept()
            ser_Dis= DataInputStream(BufferedInputStream(ser_socket?.getInputStream()))
            ser_Dos= DataOutputStream(BufferedOutputStream(ser_socket?.getOutputStream()))
            //Log.d(TAG, "ServerConnect: finish")
        }catch (_:Exception){}
        //Log.d(TAG, "ServerConnect: return")
    }

    suspend fun ClientConnect(ipaddr:String)= withContext(Dispatchers.IO){
        //Log.d(TAG, "ClientConnect: Start")
        try {
            if (cli_socket==null){
                cli_socket= Socket(ipaddr, PORT)
                cli_Dis= DataInputStream(BufferedInputStream(cli_socket?.getInputStream()))
                cli_Dos= DataOutputStream(BufferedOutputStream(cli_socket?.getOutputStream()))
                //Log.d(TAG, "ClientConnect: finish")
            }
        }catch (_:Exception){}
        //Log.d(TAG, "ClientConnect: return")
    }

    suspend fun ServerSend(ser_mes:List<ByteArray>)= withContext(Dispatchers.IO){
        //Log.d(TAG, "ServerSend: Start")
        try {
            //Log.d(TAG, "ServerSend: try")
            val firstsize=ser_mes.size
            //Log.d(TAG, "ServerSend: send first list size $firstsize")
            ser_Dos?.writeInt(firstsize)
            //Log.d(TAG, "ServerSend: $firstsize")
            ser_Dos?.flush()
            ServerReceiveNotice()
            if (ser_res_size==firstsize){
                //フラグ初期化
                ser_res_size=null
                for (mes in ser_mes){
                    ser_Dos?.writeInt(mes.size)
                    ser_Dos?.flush()
                    ServerReceiveNotice()
                    if(ser_res_size==mes.size){
                        //Log.d(TAG, "ServerSend: start send message")
                        ser_Dos?.write(mes,0,mes.size)
                        ser_Dos?.flush()
                        ServerReceiveNotice()
                        if(ser_res_size!=mes.size){
                            //Log.d(TAG, "ServerSend: miss send")
                            break
                        }
                    }
                }
                ServerReceiveEnd()
            }
        }catch (_:Exception){}
        //Log.d(TAG, "ServerSend: return")
    }

    suspend fun ServerReceiveCommonList()= withContext(Dispatchers.IO){
        //Log.d(TAG, "ServerReceiveCommonList: Start")
        try {
            ////Log.d(TAG, "ServerReceiveCommonList: try")
            ser_res_common=ser_Dis?.readBoolean()
            ////Log.d(TAG, "ServerReceiveCommonList: receive value $ser_res_common")
        }catch (_:Exception){}
        //Log.d(TAG, "ServerReceiveCommonList: return")
    }

    suspend fun ClientSend(cli_mes:List<ByteArray>)= withContext(Dispatchers.IO){
        //Log.d(TAG, "ClientSend: Start")
        try {
            //Log.d(TAG, "ClientSend: try")
            val firstlistsize=cli_mes.size
            //Log.d(TAG, "ClientSend: send first list size $firstlistsize")
            cli_Dos?.writeInt(firstlistsize)
            cli_Dos?.flush()
            ClientReceiveNotice()
            if(cli_res_size==firstlistsize){
                //フラグ初期化
                cli_res_size=null
                for (mes in cli_mes){
                    cli_Dos?.writeInt(mes.size)
                    cli_Dos?.flush()
                    ClientReceiveNotice()
                    if(cli_res_size==mes.size){
                        //Log.d(TAG, "ClientSend: Start send message")
                        cli_Dos?.write(mes,0,mes.size)
                        cli_Dos?.flush()
                        ClientReceiveNotice()
                        if(cli_res_size!=mes.size){
                            //Log.d(TAG, "ClientSend: miss send")
                            break
                        }
                    }
                }
                ClientReceiveEnd()
            }
        }catch (_:Exception){}
    }

    suspend fun ClientSendCommonlist(array: BooleanArray)= withContext(Dispatchers.IO){
        //Log.d(TAG, "ClientSendCommonlist: Start")
        try {
            //Log.d(TAG, "ClientSendCommonlist: try")
            val listsize=array.size
            cli_Dos?.writeInt(listsize)
            cli_Dos?.flush()
            ClientReceiveNotice()
            if(cli_res_size==listsize){
                //Log.d(TAG, "ClientSendCommonlist: send array")
                for (bool in array){
                    cli_Dos?.writeBoolean(bool)
                    cli_Dos?.flush()
                    ClientReceiveNotice()
                    if(cli_res_size!=1){
                        //Log.d(TAG, "ClientSendCommonlist: miss send")
                        break
                    }
                }
                ClientReceiveEnd()
            }
        }catch (_:Exception){}
        //Log.d(TAG, "ClientSendCommonlist: return")
    }



    suspend fun ServerReceiveNotice()= withContext(Dispatchers.IO){
        //Log.d(TAG, "ServerReceiveNotice: Start")
        try {
            //Log.d(TAG, "ServerReceiveNotice: try")
            ser_res_size=ser_Dis?.readInt()
            //Log.d(TAG, "ServerReceiveNotice: $ser_res_size")
            //Log.d(TAG, "ServerReceiveNotice: $ser_res_size")
        }catch (_:Exception){}
        //Log.d(TAG, "ServerReceiveNotice: return")
    }

    suspend fun ServerReceiveEnd()= withContext(Dispatchers.IO){
        //Log.d(TAG, "ServerReceiveEnd: Start")
        try {
            //Log.d(TAG, "ServerReceiveEnd: try")
            ser_end_flag= ser_Dis?.readInt()
            //Log.d(TAG, "ServerReceiveEnd: receive $ser_end_flag")
        }catch (_:Exception){}
        //Log.d(TAG, "ServerReceiveEnd: return")
    }

    suspend fun ServerReceiveSize()= withContext(Dispatchers.IO){
        //Log.d(TAG, "ServerReceive: Start")
        try {
            //Log.d(TAG, "ServerReceiveSize: try")
            ser_res_size= ser_Dis?.readInt()
            //Log.d(TAG, "ServerReceiveSize: $ser_res_size")
        }catch (_:Exception){}
        //Log.d(TAG, "ServerReceive: return")
    }

    suspend fun ServerSendNotice(flag: Int)= withContext(Dispatchers.IO){
        //Log.d(TAG, "ServerSendNotice: Start")
        try {
            //Log.d(TAG, "ServerSendNotice: try")
            ser_Dos?.writeInt(flag)
            ser_Dos?.flush()
        }catch (_:Exception){}
        //Log.d(TAG, "ServerSendNotice: return")
    }

    suspend fun ServerReceive(res_size: Int)= withContext(Dispatchers.IO){
        //Log.d(TAG, "ServerReceive: Start")
        try {
            //Log.d(TAG, "ServerReceive: try")
            ser_res_mes= ByteArray(res_size)
            ser_Dis?.read(ser_res_mes,0,res_size)
            //Log.d(TAG, "ServerReceive: message ${ser_res_mes.toString()}")
        }catch (_:Exception){}
        //Log.d(TAG, "ServerReceive: return")
    }

    suspend fun ClientReceiveEnd()= withContext(Dispatchers.IO){
        //Log.d(TAG, "ClientReceiveEnd: Start")
        try {
            //Log.d(TAG, "ClientReceiveEnd: try")
            cli_end_flag= cli_Dis?.readInt()
            //Log.d(TAG, "ClientReceiveEnd: $cli_end_flag")
        }catch (_:Exception){}
        //Log.d(TAG, "ClientReceiveEnd: return")
    }

    suspend fun ClientReceiveNotice()= withContext(Dispatchers.IO){
        //Log.d(TAG, "ClientReceiveNotice: Start")
        try {
            //Log.d(TAG, "ClientReceiveNotice: try")
            cli_res_size= cli_Dis?.readInt()
            //Log.d(TAG, "ClientReceiveNotice: $cli_res_size")
        }catch (_:Exception){}
        //Log.d(TAG, "ClientReceiveNotice: return")
    }

    suspend fun ClientReceiveSize()= withContext(Dispatchers.IO){
        //Log.d(TAG, "ClientReceiveSize: Start")
        try {
            //Log.d(TAG, "ClientReceiveSize: try")
            cli_res_size=cli_Dis?.readInt()
            //Log.d(TAG, "ClientReceiveSize: $cli_res_size")
        }catch(_:Exception){}
        //Log.d(TAG, "ClientReceiveSize: return")
    }

    suspend fun ClientSendNotice(flag:Int)= withContext(Dispatchers.IO){
        //Log.d(TAG, "ClientSendFlag: Start")
        try {
            //Log.d(TAG, "ClientSendFlag: try")
            cli_Dos?.writeInt(flag)
            cli_Dos?.flush()
        }catch (_:Exception){}
        //Log.d(TAG, "ClientSendFlag: return")
    }

    suspend fun ClientReceive(res_size:Int)= withContext(Dispatchers.IO){
        //Log.d(TAG, "ClientReceive: Start")
        try {
            //Log.d(TAG, "ClientReceive: try")
            cli_res_mes=ByteArray(res_size)
            cli_Dis?.read(cli_res_mes,0,res_size)
            //Log.d(TAG, "ClientReceive: message ${cli_res_mes.toString()}")
        }catch (_:Exception){}
        //Log.d(TAG, "ClientReceive: return")
    }

    fun DisConnectServer(){
        //Log.d(TAG, "ServerDisConnect: Start")
        ser_serversoc?.close()
        ser_socket?.close()
        ser_Dis?.close()
        ser_Dos?.close()
        //Log.d(TAG, "ServerDisConnect: return")
    }

    fun DisConnectClient(){
        //Log.d(TAG, "DisConnectClient: Start")
        cli_socket?.close()
        cli_Dis?.close()
        cli_Dos?.close()
        //Log.d(TAG, "DisConnectClient: return")
    }
}