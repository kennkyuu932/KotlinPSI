package com.example.kotlinpsi.Database

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinpsi.R
import java.time.LocalDateTime



class SearchActivity : AppCompatActivity() {

    private val contactViewModel:ContactViewModel by viewModels {
        ContactViewModel.ContactViewmodelFactory((application as ContactApplication).repository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)


        val editmonth_year=findViewById<EditText>(R.id.SearchMonth_year)
        val editmonth_month=findViewById<EditText>(R.id.SearchMonth_month)
        val editname=findViewById<EditText>(R.id.SearchName)
        val editfirst_year=findViewById<EditText>(R.id.Search_first_year)
        val editfirst_month=findViewById<EditText>(R.id.Search_first_month)
        val editfirst_day=findViewById<EditText>(R.id.Search_first_day)
        val editsecond_year=findViewById<EditText>(R.id.Search_second_year)
        val editsecond_month=findViewById<EditText>(R.id.Search_second_month)
        val editsecond_day=findViewById<EditText>(R.id.Search_second_day)
        val monthbutton=findViewById<Button>(R.id.monthsearch)
        val namebutton=findViewById<Button>(R.id.namesearch)
        val rangebutton=findViewById<Button>(R.id.rangesearch)
        val allbutton=findViewById<Button>(R.id.allsearch)
        val recycler=findViewById<RecyclerView>(R.id.recyclerView)

        val adapter = ContactListAdapter()
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)

        allbutton.setOnClickListener {
            Log.d(TAG, "onCreate: Search All")
            contactViewModel.allLists.observe(this){
                contacts -> contacts.let {
                    Log.d(TAG, "onCreate: $contacts")
                    adapter.submitList(it)
                }
                Log.d(TAG, "onCreate: observe")
            }
        }

        monthbutton.setOnClickListener {
            Log.d(TAG, "onCreate: Search month")
            val year=editmonth_year.text.toString().toInt()
            val month=editmonth_month.text.toString().toInt()
            val stop:LocalDateTime
            try{
                if(month>=1&&month<=12){
                    val start=LocalDateTime.of(year,month,1,0,0)
                    if (month==12){
                        stop=LocalDateTime.of(year+1,1,1,0,0)
                    }else{
                        stop=LocalDateTime.of(year,month+1,1,0,0)
                    }
                    contactViewModel.SearchMonth(start,stop).asLiveData().observe(this){
                        contacts -> contacts.let {
                            adapter.submitList(it)
                        }
                    }
                }else{
                    Toast.makeText(this,"正しい月を入力してください",Toast.LENGTH_SHORT).show()
                }
            }catch (_:Exception){
                Toast.makeText(this,"不正な検索の値です",Toast.LENGTH_SHORT).show()
            }
        }
        namebutton.setOnClickListener {
            Log.d(TAG, "onCreate: Search name")
            val search=editname.text.toString().toByteArray()
            try {
                contactViewModel.SearchName(search).asLiveData().observe(this){
                    contacts -> contacts.let {
                        adapter.submitList(it)
                    }
                }
            }catch (_:Exception){
                Toast.makeText(this,"不正な検索の値です",Toast.LENGTH_SHORT).show()
            }
        }
        rangebutton.setOnClickListener {
            Log.d(TAG, "onCreate: Search range")
            val first_year=editfirst_year.text.toString().toInt()
            val first_month=editfirst_month.text.toString().toInt()
            val first_day=editfirst_day.text.toString().toInt()
            val second_year=editsecond_year.text.toString().toInt()
            val second_month=editsecond_month.text.toString().toInt()
            val second_day=editsecond_day.text.toString().toInt()
            try {
                if (first_month<1 || first_month>12){
                    Toast.makeText(this,"正しい月を入力してください",Toast.LENGTH_SHORT).show()
                }else if (second_month<1 || second_month>12){
                    Toast.makeText(this,"正しい月を入力してください",Toast.LENGTH_SHORT).show()
                }else if(first_year>second_year){
                    Toast.makeText(this,"範囲がおかしいです",Toast.LENGTH_SHORT).show()
                }else{
                    val start=LocalDateTime.of(first_year,first_month,first_day,0,0)
                    val stop=LocalDateTime.of(second_year,second_month,second_day,0,0)
                    contactViewModel.SearchMonth(start,stop).asLiveData().observe(this){
                        contacts -> contacts.let {
                            adapter.submitList(it)
                        }
                    }
                }
            }catch (_:Exception){
                Toast.makeText(this,"不正な検索の値です",Toast.LENGTH_SHORT).show()
            }
        }
    }
}