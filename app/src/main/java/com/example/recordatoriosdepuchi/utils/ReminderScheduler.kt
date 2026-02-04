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

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun schedule(reminder: ReminderEntity) {
        // SEGURIDAD ANDROID 12+: Comprobar si tenemos permiso REAL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // ¡NO TENEMOS PERMISO!
                // En vez de intentar poner la alarma y crashear, avisamos al usuario.
                Toast.makeText(context, "Se requiere permiso para alarmas exactas", Toast.LENGTH_LONG).show()

                try {
                    // Abrimos la pantalla de ajustes específica para dar el permiso
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // IMPORTANTE: Salimos de la función para no ejecutar el código que provoca el crash
                return
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("AUDIO_PATH", reminder.audioPath)
            putExtra("DAYS", reminder.daysOfWeek)
            putExtra("IS_PERMANENT", reminder.isPermanent)
            putExtra("CREATION_TIME", reminder.creationTime)
            putExtra("START_H", reminder.startHour)
            putExtra("START_M", reminder.startMinute)
            putExtra("END_H", reminder.endHour)
            putExtra("END_M", reminder.endMinute)
            putExtra("REMINDER_ID", reminder.id) // ¡VITAL PARA EVITAR FANTASMAS!
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calcular siguiente disparo
        val triggerTime = calculateNextTriggerTime(reminder)

        if (triggerTime != null) {
            // AHORA ES SEGURO LLAMAR A ESTO PORQUE YA HEMOS COMPROBADO EL PERMISO
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
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("AUDIO_PATH", reminder.audioPath)
            putExtra("FORCE_TEST", true)
            putExtra("REMINDER_ID", reminder.id)
        }
        context.sendBroadcast(intent)
    }

    // LÓGICA COMPARTIDA: Calcula el milisegundo exacto del próximo sonido
    fun getNextTriggerTime(reminder: ReminderEntity): Long? {
        return calculateNextTriggerTime(reminder)
    }

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

        // 1. Si la hora de inicio ya pasó hoy, buscamos el siguiente intervalo
        if (calendar.before(now)) {
            val diff = now.timeInMillis - calendar.timeInMillis
            val intervalsPassed = diff / intervalMillis
            calendar.timeInMillis += (intervalsPassed + 1) * intervalMillis
        }

        // 2. Comprobamos si nos hemos pasado de la hora fin
        var endH = reminder.endHour
        if (endH == 0) endH = 24 // Corrección 24h

        val endCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, endH)
            set(Calendar.MINUTE, reminder.endMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Si el siguiente hueco es más tarde que la hora fin, saltamos a mañana
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