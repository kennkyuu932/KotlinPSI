package com.example.kotlinpsi.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kotlinpsi.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Contact::class], version = 10)
@TypeConverters(ContactConverter::class)
abstract class ContactDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao

    companion object{
        @Volatile
        private var INSTANCE: ContactDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ):ContactDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ContactDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()
                    .addCallback(ContactCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class ContactCallback(private val scope: CoroutineScope) :RoomDatabase.Callback(){
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populationDatabase(database.contactDao())
                    }
                }
            }
        }

        suspend fun populationDatabase(contactDao: ContactDao){
            contactDao.deleteContactAll()
        }

//        fun getInstance(context: Context): ContactDatabase {
//            return INSTANCE ?: synchronized(this){
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    ContactDatabase::class.java,
//                    "app_database"
//                ).fallbackToDestructiveMigration().build()
//                INSTANCE = instance
//                instance
//            }
//        }
    }
}