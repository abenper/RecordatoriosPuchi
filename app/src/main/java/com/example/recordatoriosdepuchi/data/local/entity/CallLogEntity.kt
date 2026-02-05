package com.example.recordatoriosdepuchi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactName: String, // Guardamos el nombre por si borran el contacto
    val timestamp: Long,
    val type: CallType, // "ENTRANTE" o "SALIENTE"
    val durationSeconds: Long = 0 // Opcional, por si queremos medir cuánto hablan
)

enum class CallType { INCOMING, OUTGOING }