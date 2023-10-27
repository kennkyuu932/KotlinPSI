package com.example.kotlinpsi.Database

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.kotlinpsi.R

class AddDataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_data)

        val addbutton=findViewById<Button>(R.id.add_database)
        val delbutton=findViewById<Button>(R.id.delete_one)
        val deleteAllbutton=findViewById<Button>(R.id.delete_all)

        addbutton.setOnClickListener {
            val editdate=findViewById<EditText>(R.id.editTextdate).getText().toString()
            val edittime=findViewById<EditText>(R.id.editTexttime).getText().toString()
            val editname=findViewById<EditText>(R.id.editTextname).getText().toString()
            AsyncTask.execute {
                val db= ContactDatabase.getInstance(this)
                db.contactDao().insertContact(Contact(date = editdate, time = edittime, name = editname))
            }
        }

        delbutton.setOnClickListener {
            val editdate=findViewById<EditText>(R.id.editTextdate).getText().toString()
            val edittime=findViewById<EditText>(R.id.editTexttime).getText().toString()
            val editname=findViewById<EditText>(R.id.editTextname).getText().toString()
            AsyncTask.execute {
                val db= ContactDatabase.getInstance(this)
                db.contactDao().deleteContact(Contact(date = editdate, time = edittime, name = editname))
            }
        }

        deleteAllbutton.setOnClickListener {
            AsyncTask.execute {
                val db= ContactDatabase.getInstance(this)
                db.contactDao().deleteContactAll()
            }
        }
    }
}