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
        Log.d(TAG, "ServerConnect: Start")
        try {
            ser_serversoc= ServerSocket(PORT)
            ser_serversoc?.reuseAddress=true
            Log.d(TAG, "ServerConnect: try")
            ser_socket= ser_serversoc?.accept()
            Log.d(TAG, "ServerConnect: finish")
        }catch (_:Exception){}
        Log.d(TAG, "ServerConnect: return")
    }

    suspend fun ClientConnect(ipaddr:String)= withContext(Dispatchers.IO){
        Log.d(TAG, "ClientConnect: Start")
        try {
            if (cli_socket==null){
                cli_socket= Socket(ipaddr, PORT)
                Log.d(TAG, "ClientConnect: finish")
            }
        }catch (_:Exception){}
        Log.d(TAG, "ClientConnect: return")
    }

    //テスト1
    suspend fun ServerSend(ser_mes:List<List<ByteArray>>)= withContext(Dispatchers.IO){
        Log.d(TAG, "ServerSend: Start")
        try {
            Log.d(TAG, "ServerSend: try")
            val first_size = ser_mes.size
            ser_Dos= DataOutputStream(BufferedOutputStream(ser_socket?.getOutputStream()))
            Log.d(TAG, "ServerSend: send frst list size $first_size")
            ser_Dos?.writeInt(first_size)
            ser_Dos?.flush()
            //クライアントからの応答を受け取る
            ServerReceiveNotice()
            if(ser_res_size==first_size){
                //フラグの初期化
                ser_res_size=null
                for (mes in ser_mes){
                    ser_Dos?.writeInt(mes.size)
                    ser_Dos?.flush()
                    ServerReceiveNotice()
                    if(ser_res_size==mes.size){
                        //フラグの初期化
                        ser_res_size=null
                        for (message in mes){
                            ser_Dos?.writeInt(message.size)
                            ser_Dos?.flush()
                            ServerReceiveNotice()
                            if(ser_res_size==message.size){
                                Log.d(TAG, "ServerSend: 最小リストのサイズ ${message.size}")
                                ser_Dos?.write(message,0,message.size)
                                ser_Dos?.flush()
                                Log.d(TAG, "ServerSend: sendmes ${message.toString()}")
                                ServerReceiveNotice()
                                if (ser_res_size!=message.size){
                                    Log.d(TAG, "ServerSend: miss send")
                                    break
                                }
                            }
                        }
                    }
                }
            }
        }catch (_:Exception){}
        Log.d(TAG, "ServerSend: return")
    }

    suspend fun ServerReceiveNotice()= withContext(Dispatchers.IO){
        Log.d(TAG, "ServerNotice: Start")
        try {
            Log.d(TAG, "ServerNotice: try")
            ser_Dis= DataInputStream(BufferedInputStream(ser_socket?.getInputStream()))
            ser_res_size=ser_Dis?.readInt()
            Log.d(TAG, "ServerNotice: $ser_res_size")
        }catch (_:Exception){}
        Log.d(TAG, "ServerNotice: return")
    }

    suspend fun ServerReceiveSize()= withContext(Dispatchers.IO){
        Log.d(TAG, "ServerReceive: Start")
        try {
            Log.d(TAG, "ServerReceiveSize: try")
            ser_Dis= DataInputStream(BufferedInputStream(ser_socket?.getInputStream()))
            ser_res_size= ser_Dis?.readInt()
            Log.d(TAG, "ServerReceiveSize: $ser_res_size")
        }catch (_:Exception){}
        Log.d(TAG, "ServerReceive: return")
    }

    suspend fun ServerSendNotice(flag: Int)= withContext(Dispatchers.IO){
        Log.d(TAG, "ServerSendNotice: Start")
        try {
            Log.d(TAG, "ServerSendNotice: try")
            ser_Dos=DataOutputStream(BufferedOutputStream(ser_socket?.getOutputStream()))
            ser_Dos?.writeInt(flag)
            ser_Dos?.flush()
        }catch (_:Exception){}
        Log.d(TAG, "ServerSendNotice: return")
    }

    suspend fun ServerReceive(res_size: Int)= withContext(Dispatchers.IO){
        Log.d(TAG, "ServerReceive: Start")
        try {
            Log.d(TAG, "ServerReceive: try")
            ser_res_mes= ByteArray(res_size)
            ser_Dis= DataInputStream(BufferedInputStream(ser_socket?.getInputStream()))
            ser_Dis?.read(ser_res_mes,0,res_size)
            Log.d(TAG, "ServerReceive: message ${ser_res_mes.toString()}")
        }catch (_:Exception){}
        Log.d(TAG, "ServerReceive: return")
    }

    suspend fun ClientSend(cli_mes:List<List<ByteArray>>)= withContext(Dispatchers.IO){
        Log.d(TAG, "ClientSend: Start")
        try {
            Log.d(TAG, "ClientSend: try")
            val first_size=cli_mes.size
            cli_Dos= DataOutputStream(BufferedOutputStream(cli_socket?.getOutputStream()))
            Log.d(TAG, "ClientSend: send first list size $first_size")
            cli_Dos?.writeInt(first_size)
            cli_Dos?.flush()
            //応答を受け取る
            ClientReceiveNotice()
            if (cli_res_size==first_size){
                cli_res_size=null
                for (mes in cli_mes){
                    cli_Dos?.writeInt(mes.size)
                    cli_Dos?.flush()
                    ClientReceiveNotice()
                    if(cli_res_size==mes.size){
                        cli_res_size=null
                        for (message in mes){
                            cli_Dos?.writeInt(message.size)
                            cli_Dos?.flush()
                            ClientReceiveNotice()
                            if(cli_res_size==message.size){
                                Log.d(TAG, "ClientSend: 最小リストのサイズ ${message.size}")
                                cli_Dos?.write(message,0,message.size)
                                cli_Dos?.flush()
                                Log.d(TAG, "ClientSend: send mes ${message.toString()}")
                                ClientReceiveNotice()
                                if( cli_res_size!=message.size){
                                    Log.d(TAG, "ClientSend: miss send")
                                    break
                                }
                            }
                        }
                    }
                }
            }
        }catch (_:Exception){}
        Log.d(TAG, "ClientSend: return")
    }

    suspend fun ClientReceiveNotice()= withContext(Dispatchers.IO){
        Log.d(TAG, "ClientReceiveNotice: Start")
        try {
            Log.d(TAG, "ClientReceiveNotice: try")
            cli_Dis= DataInputStream(BufferedInputStream(cli_socket?.getInputStream()))
            cli_res_size= cli_Dis?.readInt()
            Log.d(TAG, "ClientReceiveNotice: $cli_res_size")
        }catch (_:Exception){}
        Log.d(TAG, "ClientReceiveNotice: return")
    }

    suspend fun ClientReceiveSize()= withContext(Dispatchers.IO){
        Log.d(TAG, "ClientReceiveSize: Start")
        try {
            Log.d(TAG, "ClientReceiveSize: try")
            cli_Dis= DataInputStream(BufferedInputStream(cli_socket?.getInputStream()))
            cli_res_size=cli_Dis?.readInt()
            Log.d(TAG, "ClientReceiveSize: $cli_res_size")
        }catch(_:Exception){}
        Log.d(TAG, "ClientReceiveSize: return")
    }

    suspend fun ClientSendNotice(flag:Int)= withContext(Dispatchers.IO){
        Log.d(TAG, "ClientSendFlag: Start")
        try {
//            sleep(1000)
            Log.d(TAG, "ClientSendFlag: try")
            cli_Dos= DataOutputStream(BufferedOutputStream(cli_socket?.getOutputStream()))
            cli_Dos?.writeInt(flag)
            cli_Dos?.flush()
        }catch (_:Exception){}
        Log.d(TAG, "ClientSendFlag: return")
    }



    suspend fun ClientReceive(res_size:Int)= withContext(Dispatchers.IO){
        Log.d(TAG, "ClientReceive: Start")
        try {
            Log.d(TAG, "ClientReceive: try")
            cli_res_mes=ByteArray(res_size)
            cli_Dis= DataInputStream(BufferedInputStream(cli_socket?.getInputStream()))
            cli_Dis?.read(cli_res_mes,0,res_size)
            //Log.d(TAG, "ClientReceive: message ${cli_res_mes.let { String(it) }}")
            Log.d(TAG, "ClientReceive: message ${cli_res_mes.toString()}")
        }catch (_:Exception){}
        Log.d(TAG, "ClientReceive: return")
    }


    //

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