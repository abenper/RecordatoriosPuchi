package com.example.recordatoriosdepuchi.data

import com.example.recordatoriosdepuchi.data.local.dao.ContactDao
import com.example.recordatoriosdepuchi.data.local.dao.ReminderDao
import com.example.recordatoriosdepuchi.data.local.entity.ContactEntity
import com.example.recordatoriosdepuchi.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

class PuchiRepository(
    private val contactDao: ContactDao,
    private val reminderDao: ReminderDao
) {
    // Contactos
    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()
    suspend fun insertContact(contact: ContactEntity) = contactDao.insertContact(contact)
    suspend fun deleteContact(contact: ContactEntity) = contactDao.deleteContact(contact)
    suspend fun updateContact(contact: ContactEntity) = contactDao.updateContact(contact)

    // Recordatorios
    val allReminders: Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    suspend fun insertReminder(reminder: ReminderEntity): Long = reminderDao.insertReminder(reminder)

    suspend fun deleteReminder(reminder: ReminderEntity) = reminderDao.deleteReminder(reminder)
    suspend fun updateReminder(reminder: ReminderEntity) = reminderDao.updateReminder(reminder)
}