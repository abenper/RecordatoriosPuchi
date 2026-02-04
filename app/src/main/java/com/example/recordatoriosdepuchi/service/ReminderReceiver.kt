package com.example.recordatoriosdepuchi.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.example.recordatoriosdepuchi.data.local.entity.ReminderEntity
import com.example.recordatoriosdepuchi.service.ReminderReceiver
import java.util.Calendar

/**
 * Gestor de alarmas del sistema.
 * Se encarga de programar, cancelar y calcular los tiempos de disparo para los recordatorios de voz.
 * Maneja la compatibilidad con permisos de "Alarmas Exactas" introducidos en Android 12 (API 31).
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun schedule(reminder: ReminderEntity) {
        // GESTIÓN DE PERMISOS ANDROID 12+ (API 31)
        // Las alarmas exactas requieren un permiso especial del usuario.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Si no tenemos permiso, redirigimos al usuario a Ajustes en lugar de fallar
                Toast.makeText(context, "Se requiere permiso para alarmas exactas", Toast.LENGTH_LONG).show()
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) { e.printStackTrace() }
                return
            }
        }

        // Preparamos el Intent que recibirá el BroadcastReceiver
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("AUDIO_PATH", reminder.audioPath)
            putExtra("DAYS", reminder.daysOfWeek)
            putExtra("IS_PERMANENT", reminder.isPermanent)
            putExtra("CREATION_TIME", reminder.creationTime)
            putExtra("START_H", reminder.startHour)
            putExtra("START_M", reminder.startMinute)
            putExtra("END_H", reminder.endHour)
            putExtra("END_M", reminder.endMinute)
            putExtra("REMINDER_ID", reminder.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id, // ID único para no sobrescribir otras alarmas
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculamos cuándo debe sonar
        val triggerTime = calculateNextTriggerTime(reminder)

        if (triggerTime != null) {
            // Usamos setExactAndAllowWhileIdle para asegurar que suene incluso en modo Doze (ahorro batería)
            // Crítico para recordatorios médicos.
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancel(reminder: ReminderEntity) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun triggerTest(reminder: ReminderEntity) {
        // Envío manual de broadcast para probar el audio inmediatamente desde Admin
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("AUDIO_PATH", reminder.audioPath)
            putExtra("FORCE_TEST", true)
            putExtra("REMINDER_ID", reminder.id)
        }
        context.sendBroadcast(intent)
    }

    fun getNextTriggerTime(reminder: ReminderEntity): Long? {
        return calculateNextTriggerTime(reminder)
    }

    /**
     * Algoritmo para calcular el próximo disparo de la alarma basándose en:
     * - Hora de inicio/fin
     * - Intervalo de repetición
     * - Hora actual
     */
    private fun calculateNextTriggerTime(reminder: ReminderEntity): Long? {
        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.startHour)
            set(Calendar.MINUTE, reminder.startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val intervalMillis = reminder.intervalMinutes * 60 * 1000L
        if (intervalMillis <= 0) return null

        // Si la hora de inicio ya pasó hoy, calculamos el siguiente intervalo válido
        if (calendar.before(now)) {
            val diff = now.timeInMillis - calendar.timeInMillis
            val intervalsPassed = diff / intervalMillis
            calendar.timeInMillis += (intervalsPassed + 1) * intervalMillis
        }

        // Lógica de hora de fin (respetar el sueño del usuario)
        var endH = reminder.endHour
        if (endH == 0) endH = 24

        val endCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, endH)
            set(Calendar.MINUTE, reminder.endMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Si el siguiente recordatorio cae fuera del horario permitido, lo pasamos a mañana
        if (calendar.after(endCalendar)) {
            calendar.timeInMillis = now.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, reminder.startHour)
            calendar.set(Calendar.MINUTE, reminder.startMinute)
            calendar.set(Calendar.SECOND, 0)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }
}