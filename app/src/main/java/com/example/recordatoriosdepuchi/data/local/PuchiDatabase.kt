package com.example.recordatoriosdepuchi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.recordatoriosdepuchi.data.local.dao.ContactDao
import com.example.recordatoriosdepuchi.data.local.dao.ReminderDao
import com.example.recordatoriosdepuchi.data.local.entity.ContactEntity
import com.example.recordatoriosdepuchi.data.local.entity.ReminderEntity

@Database(entities = [ContactEntity::class, ReminderEntity::class], version = 3, exportSchema = false)
abstract class PuchiDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var Instance: PuchiDatabase? = null

        fun getDatabase(context: Context): PuchiDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, PuchiDatabase::class.java, "puchi_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}