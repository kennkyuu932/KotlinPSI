package com.example.kotlinpsi.ble

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.activity.viewModels
import com.example.kotlinpsi.MainActivity
import com.example.kotlinpsi.Transmission.ClientActivity
import com.example.kotlinpsi.Transmission.ServerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BLEControl {
    private lateinit var bluetoothManager: BluetoothManager

    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private var gattServer: BluetoothGattServer? = null
    private var gattServerCallback: BluetoothGattServerCallback? = null

    private var advertiser: BluetoothLeAdvertiser? = null
    private var advertiseCallback: AdvertiseCallback? = null
    private var advertiseSettings: AdvertiseSettings = buildAdvertiseSettings()
    private var advertiseData: AdvertiseData = buildAdvertiseData()

    private var gattClient: BluetoothGatt? = null
    private var gattClientCallback: BluetoothGattCallback? = null

    private var gatt: BluetoothGatt? = null
    private var messageCharacteristic: BluetoothGattCharacteristic? = null

    private var Testmessage: String = "test!!"

    public lateinit var se_name:String
    public lateinit var se_ip:String

    fun startServer(app: Application,kamei:Int){
        bluetoothManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if(!adapter.isEnabled){
            Log.d(ContentValues.TAG, "startServer: bluetooth unable")
        }else{
            setupGattServer(app)
            startAdvertisement(app,kamei)
            Log.d(ContentValues.TAG, "startServer: advertise")
            Log.d(ContentValues.TAG, "startServer: ${adapter.address}")
        }
    }

    fun stopServer(app: Application){
        stopAdvertising(app)
    }
    private fun setupGattServer(app: Application){
        gattServerCallback=GattServerCallback(app)

        if (ActivityCompat.checkSelfPermission(app,
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        gattServer= bluetoothManager.openGattServer(app, gattServerCallback).apply {
            addService(setupGattService())
        }
    }

    private fun setupGattService(): BluetoothGattService {
        val service= BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val messageCharacteristic= BluetoothGattCharacteristic(
            MESSAGE_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(messageCharacteristic)
        val confirmCharacteristic= BluetoothGattCharacteristic(
            CONFIRM_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(confirmCharacteristic)
        return service
    }

    private fun startAdvertisement(app: Application,kamei:Int){
        if (ActivityCompat.checkSelfPermission(app,Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        adapter.setName(kamei.toString())
        Log.d(ContentValues.TAG, "startAdvertisement: +${adapter.name}")
        advertiser= adapter.bluetoothLeAdvertiser
        Log.d(ContentValues.TAG, "startAdvertisement: with advertiser $advertiser")

        if(advertiseCallback==null){
            advertiseCallback = DeviceAdvertiseCallback()

            if (ActivityCompat.checkSelfPermission(app,
                    Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            advertiser?.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
        }
    }

    private fun stopAdvertising(app: Application){
        Log.d(ContentValues.TAG, "stopAdvertising: stop advertiser $advertiser")
        if (ActivityCompat.checkSelfPermission(app,
                Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        advertiser?.stopAdvertising(advertiseCallback)
        advertiseCallback=null
    }

    private fun buildAdvertiseData(): AdvertiseData {
        val dataBuilder= AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .setIncludeDeviceName(true)

        return dataBuilder.build()
    }

    private fun buildAdvertiseSettings(): AdvertiseSettings {
        return AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setTimeout(0)
            .build()
    }

    fun connectToChatDevice(device: BluetoothDevice, app: Application,ipaddr:String){
        gattClientCallback=GattClientCallback(app)
        if (ActivityCompat.checkSelfPermission(app, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        gattClient=device.connectGatt(app,false, gattClientCallback)
        sendMessage(ipaddr,app)
    }

    fun sendMessage(message: String,app: Application): Boolean{
        Log.d(ContentValues.TAG, "sendMessage: start")
        messageCharacteristic?.let { characteristic ->
            characteristic.writeType= BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

            val messageBytes=message.toByteArray(Charsets.UTF_8)
            characteristic.value=messageBytes
            gatt?.let {
                if (ActivityCompat.checkSelfPermission(app, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
                val success = it.writeCharacteristic(messageCharacteristic)
                Log.d(ContentValues.TAG, "sendMessage: send")
            }?: kotlin.run {
                Log.d(ContentValues.TAG, "sendMessage: no gatt connection")
            }
        }
        return true
    }

    fun connectfromaddress(address: String,app: Application,ipaddr: String){
        val device: BluetoothDevice
        adapter.let { adapter ->
            device = adapter.getRemoteDevice(address)
        }
        connectToChatDevice(device,app,ipaddr)
    }

    private class DeviceAdvertiseCallback : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            val errorMessage = "Advertise failed with error: $errorCode"
            Log.d(ContentValues.TAG, "onStartFailure: Advertising Failed")
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d(ContentValues.TAG, "onStartSuccess: Advertising successfully started")
        }
    }

    private class GattServerCallback(val application: Application) : BluetoothGattServerCallback(){
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            val isSuccess=status== BluetoothGatt.GATT_SUCCESS
            val isConnected=newState== BluetoothProfile.STATE_CONNECTED
        }

        val handler=Handler(Looper.getMainLooper())
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            if (characteristic != null) {
                if(characteristic.uuid== MESSAGE_UUID){
                    if (ActivityCompat.checkSelfPermission(application, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return
                    }
                    gattServer?.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,0,null)
                    val message = value?.toString(Charsets.UTF_8)
                    Log.d(ContentValues.TAG, "onCharacteristicWriteRequest: Have message: $message")
                    val len=message?.length
                    val name = message?.dropLast(11)
                    val ipadd= name?.let { message.drop(it.length) }
                    Log.d(ContentValues.TAG, "onCharacteristicWriteRequest: name : $name ipaddress : $ipadd")
                    if (name != null) {
                        se_name=name
                    }
                    if (ipadd != null) {
                        se_ip=ipadd
                    }
                    MainActivity.bleViewModel.bleflag.postValue(1)
//                    AlertDialog.Builder(application)
//                        .setTitle(name+"との接触回数を表示しますか(クライアント)")
//                        .setPositiveButton("Start"){dialog,which ->
//                            Log.d(ContentValues.TAG, "onCharacteristicWriteRequest: $ipadd")
//                        }.setNegativeButton("Cancel"){dialog,which ->}.show()
                }
            }
        }
    }

    private class GattClientCallback(val application: Application): BluetoothGattCallback(){

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val isSuccess=status== BluetoothGatt.GATT_SUCCESS
            val isConnected=newState== BluetoothProfile.STATE_CONNECTED
            Log.d(ContentValues.TAG, "onConnectionStateChange: Client $gatt  success: $isSuccess  connected: $isConnected")
            if (isSuccess&&isConnected){
                if (ActivityCompat.checkSelfPermission(application, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(ContentValues.TAG, "onConnectionStateChange: permission")
                    return
                }
                Log.d(ContentValues.TAG, "onConnectionStateChange: gattdiscoverservice")
                gatt?.discoverServices()
            }
        }

        override fun onServicesDiscovered(discoveredgatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(discoveredgatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS){
                Log.d(ContentValues.TAG, "onServicesDiscovered: Have gatt $discoveredgatt")
                gatt=discoveredgatt
                val service=discoveredgatt?.getService(SERVICE_UUID)
                if (service != null) {
                    messageCharacteristic = service.getCharacteristic(MESSAGE_UUID)
                }
            }
        }
    }

}