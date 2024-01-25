package com.example.kotlinpsi

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraExtensionSession.StillCaptureLatency
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinpsi.Database.AddDataActivity
import com.example.kotlinpsi.Database.Contact
import com.example.kotlinpsi.Database.ContactApplication
import com.example.kotlinpsi.Database.ContactViewModel
import com.example.kotlinpsi.Transmission.ClientActivity
import com.example.kotlinpsi.Transmission.ServerActivity
import com.example.kotlinpsi.ble.BLEControl
import com.example.kotlinpsi.ble.BLEViewModel
import com.example.kotlinpsi.ble.SERVICE_UUID
import com.example.kotlinpsi.databinding.ActivityMainBinding
import java.net.Inet4Address
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var ipaddr_before : String

    private lateinit var ipadd : String


    private var scanCallback: DeviceScanCallback?=null

    private val scanFilters: List<ScanFilter>

    private val scanSettings: ScanSettings

    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private var scanner: BluetoothLeScanner? = null

    private val scanResults = mutableMapOf<String, BluetoothDevice>()

    private val scanlist = mutableListOf<ScanRecyclerAdapter.Item>()

    private var radio = 0


    private lateinit var recyclerView: RecyclerView
    private lateinit var radapter: ScanRecyclerAdapter

    private val contactViewModel: ContactViewModel by viewModels {
        ContactViewModel.ContactViewmodelFactory((application as ContactApplication).repository)
    }



    init {
        scanFilters = buildScanFilters()
        scanSettings = buildScanSettings()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val server_ip_text_main=findViewById<TextView>(R.id.server_ip_main)
//        var radio=0

        bleViewModel = BLEViewModel()


        val connectivityManager = getSystemService(ConnectivityManager::class.java)

        val networkCallback = object : ConnectivityManager.NetworkCallback(){
            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties)
                ipaddr_before=linkProperties.linkAddresses.filter {
                    it.address is Inet4Address
                }[0].toString()
                ipadd=ipaddr_before.removeRange(11,14)
                server_ip_text_main.text=ipadd
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        val database = binding.toAdddata
        database.setOnClickListener {
            val intent=Intent(this, AddDataActivity::class.java)
            startActivity(intent)
        }

        val kamei=findViewById<TextView>(R.id.kamei)
//        val range = (0..10000)
//        val kana = intent.getIntExtra("kamei_activity",0)
        kamei.setText("あなたの仮名は"+StartActivity.kamei+"です")


        val ip_text=findViewById<EditText>(R.id.edit_ip)

        val serbutton=binding.serverPsi
        serbutton.setOnClickListener {
            //Log.d(TAG, "onCreate: push server psi button")
            val intent=Intent(this,ServerActivity::class.java)
            intent.putExtra(radioflag,radio)
            startActivity(intent)
        }
        val clibutton=binding.clientPsi
        clibutton.setOnClickListener {
            //Log.d(TAG, "onCreate: push client psi button")
            val intent=Intent(this,ClientActivity::class.java)
            intent.putExtra(server_ip,ip_text.text.toString())
            intent.putExtra(radioflag,radio)
            startActivity(intent)
        }


        val radio1month=binding.radioMonthlater
        radio1month.setOnClickListener {
            //Log.d(TAG, "onCreate: radio 1")
            radio=1
        }
        val radio3month=binding.radio3monthlater
        radio3month.setOnClickListener {
            //Log.d(TAG, "onCreate: radio 3")
            radio=3
        }
        val radioall=binding.radioAll
        radioall.setOnClickListener {
            //Log.d(TAG, "onCreate: radio 0")
            radio=0
        }

        //Scan動作
        startScan(application)

        bleViewModel.bleflag.observe(this){
            ble -> ble.let {
                if (ble==1){
                    AlertDialog.Builder(this)
                        .setTitle(BLEControl.se_name+"との接触回数を表示しますか(クライアント)")
                        .setPositiveButton("Start"){dialog,which ->
                            Log.d(TAG, "ip : ${BLEControl.se_ip}")
                            val intent = Intent(this,ClientActivity::class.java)
                            intent.putExtra(server_ip,BLEControl.se_ip)
                            intent.putExtra(radioflag,radio)
                            startActivity(intent)
                        }.setNegativeButton("Cancel"){dialog,which ->}.show()
                }
            }
        }

    }

    private fun handleItemClick(item: ScanRecyclerAdapter.Item){
        val device = item.device
        val address : String = device.address
        Log.d(TAG, "handleItemClick: $address")
        //ChatServer.connectToChatDevice(device,application)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        AlertDialog.Builder(this)
            .setTitle(device.name+"との接触回数を表示しますか(サーバ)")
            .setPositiveButton("Start"){dialog,which ->
                val message=device.name+ipadd
                BLEControl.connectfromaddress(address,application,message)
                val intent = Intent(this,ServerActivity::class.java)
                intent.putExtra(radioflag,radio)
                startActivity(intent)
            }.setNegativeButton("Cancel"){dialog,which ->}.show()
    }

    fun startScan(app: Application){
        if(!adapter.isMultipleAdvertisementSupported){
            Log.d(TAG, "startScan: failed")
            return
        }
        if (scanCallback==null){
            scanner=adapter.bluetoothLeScanner
            Log.d(TAG, "startScan: start scanning")

            Handler(Looper.myLooper()!!).postDelayed({stopScanning(app)}, Companion.SCAN_PERIOD)

            scanCallback=DeviceScanCallback()

            if (ActivityCompat.checkSelfPermission(app, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "startScan: permission denied")
                return
            }
            scanner?.startScan(scanFilters,scanSettings,scanCallback)
        }
    }

    private fun stopScanning(app: Application){
        Log.d(TAG, "stopScanning: stop scanning")
        Log.d(TAG, "stopScanning: "+scanResults.keys)

        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        for (map in scanResults){
            scanlist.add(ScanRecyclerAdapter.Item(map.value.address,map.value.name,map.value))

            //データの追加
            contactViewModel.insert(Contact(now,map.value.name.toByteArray()))
        }

        recyclerView=findViewById(R.id.scanrecycler)
        recyclerView.layoutManager= LinearLayoutManager(applicationContext)
        Log.d(TAG, "adapter set")
        radapter = ScanRecyclerAdapter(scanlist){ item ->
            handleItemClick(item)
            Log.d(TAG, "onCreate: itemclick")
        }
        recyclerView.adapter=radapter
        if (ActivityCompat.checkSelfPermission(app, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        scanner?.stopScan(scanCallback)
        scanCallback=null
    }

    private fun buildScanFilters(): List<ScanFilter>{
        val builder = ScanFilter.Builder()
        builder.setServiceUuid(ParcelUuid(SERVICE_UUID))
        val filter = builder.build()
        return listOf(filter)
    }

    private fun buildScanSettings(): ScanSettings{
        return ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
    }

    private inner class DeviceScanCallback : ScanCallback(){
        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            for (item in results) {
                item.device?.let { device ->
                    scanResults[device.address] = device
                }
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (result != null) {
                result.device?.let { device->
                    scanResults[device.address]=device
                    //データベース追加
                    if (ActivityCompat.checkSelfPermission(application, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return
                    }
//                    val now=LocalDateTime.now()
//                    Log.d(TAG, "onScanResult: $now")
//                    Log.d(TAG, "onScanResult: insert")
                    //contactViewModel.insert(Contact(LocalDateTime.now(),device.name.toByteArray()))
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            val errorMessage = "Scan failed with error $errorCode"
        }
    }

    /**
     * A native method that is implemented by the 'kotlinpsi' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun Boringtest(): Int

    //1端末でPSIのデモ
    external fun OneCryptoMessage(message:String,message_cl:String): String



    companion object {
        // Used to load the 'kotlinpsi' library on application startup.
        init {
            System.loadLibrary("kotlinpsi")
        }
        val server_intent_message = "SERVRMESSAGE"
        val client_intent_message = "CLIENTMESSAGE"
        val psi_intent_message = "PSIRESULT"

        val server_ip = "SERVERIP"
        val radioflag = "RADIOBUTTON"

        //時間計測のためのTAG
        val TAG_TIME = "TIME_to_PSI"

        var encrypt_start_first = 0L
        var encrypt_finish_first = 0L
        var send_start_first = 0L
        var send_finish_first = 0L
        var receive_start_first = 0L
        var receive_finish_first = 0L
        var encrypt_start_second = 0L
        var encrypt_finish_second = 0L
        var send_start_second = 0L
        var send_finish_second = 0L
        var receive_start_second = 0L
        var receive_finish_second = 0L
        var server_PSI_second_start = 0L
        var server_PSI_second_finish = 0L
        var client_PSI_second_start = 0L
        var client_PSI_second_finish = 0L
        var server_data_read_start=0L
        var server_data_read_finish=0L
        var client_data_read_start=0L
        var client_data_read_finish=0L

        var server_send_first2=0L
        var server_receive_first2=0L
        var server_send_second2=0L
        var server_receive_second2=1L
        var test_start_server=0L
        var test_finish_server=0L
        var client_send_first2=0L
        var client_receive_first2=0L
        var client_send_second2=1L
        var client_receive_second2=0L
        var test_start_client=0L
        var test_finish_client=0L

        public lateinit var test:String

        private const val SCAN_PERIOD = 10000L

        lateinit var bleViewModel: BLEViewModel
    }
}