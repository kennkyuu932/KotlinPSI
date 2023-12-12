package com.example.kotlinpsi.Database

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.kotlinpsi.MainActivity
import com.example.kotlinpsi.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

class AddDataActivity : AppCompatActivity() {

    lateinit var editdate:EditText
    lateinit var editname:EditText
    var pickyear:Int = 0
    var pickmonth:Int=0
    var pickday:Int=0
    var pickhour:Int=0
    var pickminute:Int=0

    private val contactviewmodel : ContactViewModel by viewModels {
        ContactViewModel.ContactViewmodelFactory((application as ContactApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_data)




        val addbutton=findViewById<Button>(R.id.add_database)
        val delbutton=findViewById<Button>(R.id.delete_one)
        val deleteAllbutton=findViewById<Button>(R.id.delete_all)
        val datepick=findViewById<Button>(R.id.date_pick)
        editdate=findViewById(R.id.editTextdate)
        editname=findViewById(R.id.editTextname)

        val searchbutton=findViewById<Button>(R.id.search)
        searchbutton.setOnClickListener {
            val intent: Intent = Intent(this,SearchActivity::class.java)
            startActivity(intent)
        }

//        val db= ContactDatabase.getInstance(this)
        datepick.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            val datepickerdialog= DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    pickyear=year
                    pickmonth=month
                    pickday=dayOfMonth
                    editdate.setText("${pickyear.toString().padStart(4,'0')}" +
                            "-${(pickmonth+1).toString().padStart(2,'0')}" +
                            "-${pickday.toString().padStart(2,'0')} " +
                            "${pickhour.toString().padStart(2,'0')}:" +
                            "${pickminute.toString().padStart(2,'0')}")
                },currentYear,currentMonth,currentDay)
            datepickerdialog.show()

            val timePickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    // 時間をテキストボックスに表示
                    pickhour=hourOfDay
                    pickminute=minute
                    editdate.setText("${pickyear.toString().padStart(4,'0')}" +
                            "-${(pickmonth+1).toString().padStart(2,'0')}" +
                            "-${pickday.toString().padStart(2,'0')} " +
                            "${pickhour.toString().padStart(2,'0')}:" +
                            "${pickminute.toString().padStart(2,'0')}")
                },
                currentHour,
                currentMinute,
                true
            )
            timePickerDialog.show()
        }

        addbutton.setOnClickListener {
            val editdatetext=editdate.text.toString()
            val editnametext=editname.text.toString().toByteArray()
            try {
                val formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val roomdate=LocalDateTime.parse(editdatetext,formatter)
                contactviewmodel.insert(Contact(date = roomdate, name = editnametext))
                Log.d(TAG, "onCreate: 要素を追加")
                Log.d(TAG, "onCreate: name=${editnametext}")
                Toast.makeText(this,"要素を追加",Toast.LENGTH_SHORT).show()
            }catch (_:Exception){
                Log.d(TAG, "onCreate: データベースへの入力に失敗")
                Toast.makeText(this,"要素を追加に失敗",Toast.LENGTH_SHORT).show()
            }
        }

        delbutton.setOnClickListener {
            val editdatetext=editdate.text.toString()
            val editnametext=editname.text.toString().toByteArray()
            try {
                val formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val roomdate=LocalDateTime.parse(editdatetext,formatter)
                contactviewmodel.deletecontact(Contact(date = roomdate, name = editnametext))
                Log.d(TAG, "onCreate: 要素を削除")
                Toast.makeText(this,"要素を削除",Toast.LENGTH_SHORT).show()
            }catch (_:Exception){
                Log.d(TAG, "onCreate: データベースへの要素の削除に失敗")
                Toast.makeText(this,"要素の削除に失敗",Toast.LENGTH_SHORT).show()
            }
        }

        deleteAllbutton.setOnClickListener {
            try {
                contactviewmodel.deleteAll()
                Log.d(TAG, "onCreate: テーブル削除")
                Toast.makeText(this,"テーブル削除",Toast.LENGTH_SHORT).show()
            }catch (_:Exception){
                Log.d(TAG, "onCreate: テーブルの削除に失敗")
                Toast.makeText(this,"テーブルの削除に失敗",Toast.LENGTH_SHORT).show()
            }
        }
    }
}