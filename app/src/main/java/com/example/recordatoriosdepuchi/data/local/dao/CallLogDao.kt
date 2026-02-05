package com.example.recordatoriosdepuchi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.recordatoriosdepuchi.data.local.entity.CallLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    @Insert
    suspend fun insertLog(log: CallLogEntity)

    // RA5.b y RA5.c: Fuente de datos y filtros
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getLogsSince(startTime: Long): Flow<List<CallLogEntity>>

    // RA5.d: Valores calculados (Totales)
    @Query("SELECT COUNT(*) FROM call_logs")
    suspend fun getTotalCalls(): Int
}