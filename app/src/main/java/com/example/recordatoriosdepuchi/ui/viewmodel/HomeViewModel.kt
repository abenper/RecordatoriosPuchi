package com.example.recordatoriosdepuchi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recordatoriosdepuchi.data.PuchiRepository
import com.example.recordatoriosdepuchi.data.local.entity.ContactEntity
import com.example.recordatoriosdepuchi.data.local.entity.ReminderEntity
import com.example.recordatoriosdepuchi.utils.PreferenceHelper
import com.example.recordatoriosdepuchi.utils.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Collections
import com.example.recordatoriosdepuchi.data.local.entity.CallLogEntity
import com.example.recordatoriosdepuchi.data.local.entity.CallType

class HomeViewModel(
    application: Application,
    private val repository: PuchiRepository,
    private val scheduler: ReminderScheduler
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // Trigger para refrescar la lista al reordenar
    private val _contactOrderTrigger = MutableStateFlow(0)

    val contacts: StateFlow<List<ContactEntity>> = combine(
        repository.allContacts,
        _contactOrderTrigger
    ) { dbContacts, _ ->
        val orderIds = PreferenceHelper.getContactOrder(context) // Ahora devuelve List<Int>
        if (orderIds.isEmpty()) {
            dbContacts
        } else {
            dbContacts.sortedBy { contact ->
                // Buscamos el ID (Int) en la lista de orden (Int)
                val index = orderIds.indexOf(contact.id)
                if (index == -1) Int.MAX_VALUE else index
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderEntity>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estado del test de salvapantallas
    private val _isScreensaverTest = MutableStateFlow(false)
    val isScreensaverTest = _isScreensaverTest.asStateFlow()

    fun startScreensaverTest() { _isScreensaverTest.value = true }
    fun stopScreensaverTest() { _isScreensaverTest.value = false }

    // --- REORDENACIÓN ---
    fun moveContactUp(contact: ContactEntity, currentList: List<ContactEntity>) {
        val index = currentList.indexOf(contact)
        if (index > 0) {
            val newList = currentList.toMutableList()
            Collections.swap(newList, index, index - 1)
            saveOrder(newList)
        }
    }

    fun moveContactDown(contact: ContactEntity, currentList: List<ContactEntity>) {
        val index = currentList.indexOf(contact)
        if (index < currentList.size - 1) {
            val newList = currentList.toMutableList()
            Collections.swap(newList, index, index + 1)
            saveOrder(newList)
        }
    }

    private fun saveOrder(list: List<ContactEntity>) {
        val ids = list.map { it.id } // Esto crea una List<Int>
        PreferenceHelper.setContactOrder(context, ids) // Ahora coincide con el tipo esperado
        _contactOrderTrigger.value += 1
    }

    // --- CRUD ---
    fun addContact(name: String, number: String, photoUri: String) {
        viewModelScope.launch {
            repository.insertContact(ContactEntity(name = name, phoneNumber = number, photoUri = photoUri))
        }
    }

    fun deleteContact(contact: ContactEntity) { viewModelScope.launch { repository.deleteContact(contact) } }
    fun updateContact(contact: ContactEntity) { viewModelScope.launch { repository.updateContact(contact) } }

    // --- RECORDATORIOS ---
    fun addAdvancedReminder(
        name: String, audioPath: String, startH: Int, startM: Int,
        endH: Int, endM: Int, interval: Int, permanent: Boolean, daysOfWeek: List<Int>
    ) {
        val daysString = daysOfWeek.joinToString(",")
        viewModelScope.launch {
            val reminder = ReminderEntity(
                name = name, audioPath = audioPath,
                startHour = startH, startMinute = startM,
                endHour = endH, endMinute = endM,
                intervalMinutes = interval, isPermanent = permanent,
                daysOfWeek = daysString, creationTime = System.currentTimeMillis()
            )
            val newId = repository.insertReminder(reminder)
            val savedReminder = reminder.copy(id = newId.toInt())
            scheduler.schedule(savedReminder)
        }
    }

    fun updateReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
            scheduler.cancel(reminder)
            scheduler.schedule(reminder)
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            scheduler.cancel(reminder)
        }
    }

    fun testReminder(reminder: ReminderEntity) {
        scheduler.triggerTest(reminder)
    }

    val callLogs: StateFlow<List<CallLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Función para registrar llamadas (llámala cuando se cuelgue o se inicie llamada)
    fun registerCall(contactName: String, isIncoming: Boolean) {
        viewModelScope.launch {
            val type = if (isIncoming) CallType.INCOMING else CallType.OUTGOING
            repository.logCall(contactName, type)
        }
    }
}

class HomeViewModelFactory(
    private val application: Application,
    private val repository: PuchiRepository,
    private val scheduler: ReminderScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(application, repository, scheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}