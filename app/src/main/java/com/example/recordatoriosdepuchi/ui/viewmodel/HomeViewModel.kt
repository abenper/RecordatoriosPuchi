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

/**
 * ViewModel principal que gestiona el estado de la UI y la comunicación con la capa de datos.
 * Utiliza StateFlow para un flujo de datos reactivo y seguro para el ciclo de vida.
 */
class HomeViewModel(
    application: Application,
    private val repository: PuchiRepository,
    private val scheduler: ReminderScheduler
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // GESTIÓN DE ESTADO

    // Trigger para forzar la recomposición de la lista cuando cambiamos el orden manual.
    private val _contactOrderTrigger = MutableStateFlow(0)

    /**
     * Flujo de contactos ordenados.
     * Combina la base de datos (Room) con las preferencias de orden del usuario.
     * Esto permite una personalización total de la interfaz sin alterar la estructura de la DB.
     */
    val contacts: StateFlow<List<ContactEntity>> = combine(
        repository.allContacts,
        _contactOrderTrigger
    ) { dbContacts, _ ->
        val orderIds = PreferenceHelper.getContactOrder(context)
        if (orderIds.isEmpty()) {
            dbContacts // Orden por defecto si no hay preferencias
        } else {
            // Algoritmo de ordenación basado en lista de IDs guardada
            dbContacts.sortedBy { contact ->
                val index = orderIds.indexOf(contact.id.toString())
                if (index == -1) Int.MAX_VALUE else index // Los nuevos contactos van al final
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderEntity>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estado para controlar la vista previa del Asistente Virtual en el panel de Admin
    private val _isScreensaverTest = MutableStateFlow(false)
    val isScreensaverTest = _isScreensaverTest.asStateFlow()

    fun startScreensaverTest() { _isScreensaverTest.value = true }
    fun stopScreensaverTest() { _isScreensaverTest.value = false }

    // LÓGICA DE NEGOCIO: REORDENACIÓN

    fun moveContactUp(contact: ContactEntity, currentList: List<ContactEntity>) {
        val index = currentList.indexOf(contact)
        if (index > 0) {
            val newList = currentList.toMutableList()
            java.util.Collections.swap(newList, index, index - 1)
            saveOrder(newList)
        }
    }

    fun moveContactDown(contact: ContactEntity, currentList: List<ContactEntity>) {
        val index = currentList.indexOf(contact)
        if (index < currentList.size - 1) {
            val newList = currentList.toMutableList()
            java.util.Collections.swap(newList, index, index + 1)
            saveOrder(newList)
        }
    }

    private fun saveOrder(list: List<ContactEntity>) {
        val ids = list.map { it.id }
        PreferenceHelper.setContactOrder(context, ids)
        _contactOrderTrigger.value += 1 // Notifica a la UI que debe repintarse
    }

    // OPERACIONES CRUD (BASE DE DATOS)
    // Usamos viewModelScope para lanzar corrutinas que sobreviven a cambios de configuración.

    fun addContact(name: String, number: String, photoUri: String) {
        viewModelScope.launch {
            repository.insertContact(ContactEntity(name = name, phoneNumber = number, photoUri = photoUri))
        }
    }

    fun deleteContact(contact: ContactEntity) { viewModelScope.launch { repository.deleteContact(contact) } }

    fun updateContact(contact: ContactEntity) { viewModelScope.launch { repository.updateContact(contact) } }

    // GESTIÓN DE RECORDATORIOS Y ALARMAS

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
            // Insertamos en DB y obtenemos el ID para programar la alarma
            val newId = repository.insertReminder(reminder)
            val savedReminder = reminder.copy(id = newId.toInt())
            scheduler.schedule(savedReminder)
        }
    }

    fun updateReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
            // Reprogramamos la alarma para aplicar cambios
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
}

/**
 * Factory para inyectar dependencias en el ViewModel.
 * Necesario porque el ViewModel por defecto no acepta parámetros en el constructor.
 */
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