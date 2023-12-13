package com.example.kotlinpsi.Database

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.documentfile.provider.DocumentFile
import com.example.kotlinpsi.R
import com.opencsv.CSVReader
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.log

class ReadCSVActivity : AppCompatActivity() {
    
    val READ_REQUEST_CODE = 42
    
    lateinit var uri: Uri

    lateinit var textfilename:TextView

    val formatter= DateTimeFormatter.ofPattern("yyyy/MM/dd H:mm")

    private val contactviewmodel : ContactViewModel by viewModels {
        ContactViewModel.ContactViewmodelFactory((application as ContactApplication).repository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_csvactivity)

        val selectcsvbutton=findViewById<Button>(R.id.selectfilebutton)
        val addcsvbutton=findViewById<Button>(R.id.add_csvbutton)
        val changecsvbutton=findViewById<Button>(R.id.change_csvbutton)
        val delcsvbutton=findViewById<Button>(R.id.del_csvbutton)
        textfilename=findViewById(R.id.selectfilename)


        selectcsvbutton.setOnClickListener {
            Log.d(TAG, "onCreate: select button pushed")
            selectcsv()
        }

        addcsvbutton.setOnClickListener {
            try {
                addData(uri)
                Toast.makeText(this,"データベースへ追加",Toast.LENGTH_SHORT).show()
            }catch (_:Exception){
                Toast.makeText(this,"データベースへ追加を失敗",Toast.LENGTH_SHORT).show()
            }
        }

        changecsvbutton.setOnClickListener {
            try {
                changeData(uri)
                Toast.makeText(this,"データベースを入れ替え",Toast.LENGTH_SHORT).show()
            }catch (_:Exception){
                Toast.makeText(this,"データベース入れ替え失敗",Toast.LENGTH_SHORT).show()
            }
        }

        delcsvbutton.setOnClickListener {
            try {
                delData(uri)
                Toast.makeText(this,"データベースの一致した要素を削除",Toast.LENGTH_SHORT).show()
            }catch (_:Exception){
                Toast.makeText(this,"データベースの一致した要素の削除に失敗",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectcsv(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("text/comma-separated-values")
        startActivityForResult(intent,READ_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==READ_REQUEST_CODE && resultCode== Activity.RESULT_OK) run {
            uri = data?.data!!
            Log.d(TAG, "onActivityResult: uri toString${uri}")
        }
        val mime = contentResolver.getType(uri)
        //Uriからパスに変換
        val documentFile=DocumentFile.fromSingleUri(this,uri)
        documentFile?.let {
            val name = it.name
            val filepath=it.uri.path.toString()
            textfilename.setText(name)
            Log.d(TAG, "onActivityResult: $filepath")
        }
    }

    private fun addData(uri: Uri){
        Log.d(TAG, "addData: Start")
        val inputstream = contentResolver.openInputStream(uri)
        inputstream?.use { stream ->
            val reader = CSVReader(InputStreamReader(stream))
            val lines=reader.readAll()
            for (line in lines){
                val date_s = line[0].toString()
                val name_s = line[1].toString()
                val date = LocalDateTime.parse(date_s,formatter)
                val name=name_s.toByteArray()
                contactviewmodel.insert(Contact(date,name))
            }
        }
    }

    private fun changeData(uri:Uri){
        Log.d(TAG, "changeData: Start")
        try {
            contactviewmodel.deleteAll()
            addData(uri)
        }catch (_:Exception){}
    }

    private fun delData(uri:Uri){
        Log.d(TAG, "addData: Start")
        val inputstream = contentResolver.openInputStream(uri)
        inputstream?.use { stream ->
            val reader = CSVReader(InputStreamReader(stream))
            val lines=reader.readAll()
            for (line in lines){
                val date_s = line[0].toString()
                val name_s = line[1].toString()
                val date = LocalDateTime.parse(date_s,formatter)
                val name=name_s.toByteArray()
                contactviewmodel.deletecontact(Contact(date,name))
            }
        }
    }



}