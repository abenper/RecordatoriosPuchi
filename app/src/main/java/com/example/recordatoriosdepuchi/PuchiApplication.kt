package com.example.recordatoriosdepuchi

import android.app.Application
import com.example.recordatoriosdepuchi.data.PuchiRepository
import com.example.recordatoriosdepuchi.data.local.PuchiDatabase

class PuchiApplication : Application() {

    val database by lazy { PuchiDatabase.getDatabase(this) }
    val repository by lazy { PuchiRepository(database.contactDao(), database.reminderDao()) }
}