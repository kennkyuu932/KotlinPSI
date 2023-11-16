package com.example.kotlinpsi.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.kotlinpsi.MainActivity

@Database(entities = [Contact::class], version = 4)
@TypeConverters(ContactConverter::class)
abstract class ContactDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao

    companion object{
        @Volatile
        private var INSTANCE: ContactDatabase? = null

        fun getInstance(context: Context): ContactDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ContactDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}