package com.example.recordatoriosdepuchi.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.recordatoriosdepuchi.data.local.PuchiDatabase
import com.example.recordatoriosdepuchi.utils.PreferenceHelper
import com.example.recordatoriosdepuchi.utils.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        private var currentMediaPlayer: MediaPlayer? = null
        private var loudnessEnhancer: LoudnessEnhancer? = null

        fun stopCurrentAudio() {
            try {
                if (currentMediaPlayer?.isPlaying == true) {
                    currentMediaPlayer?.stop()
                }
                currentMediaPlayer?.release()
                loudnessEnhancer?.release()
                currentMediaPlayer = null
                loudnessEnhancer = null
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "Puchi:AudioWakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L)

        val audioPath = intent.getStringExtra("AUDIO_PATH")
        val reminderId = intent.getIntExtra("REMINDER_ID", -1)

        val db = PuchiDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            if (reminderId != -1) {
                val reminder = db.reminderDao().getReminderById(reminderId)
                if (reminder != null) {
                    val scheduler = ReminderScheduler(context)
                    scheduler.schedule(reminder)
                } else {
                    Handler(Looper.getMainLooper()).post { finishWork(pendingResult) }
                    return@launch
                }
            }

            Handler(Looper.getMainLooper()).post {
                processAlarm(context, intent, audioPath, pendingResult)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun processAlarm(context: Context, intent: Intent, audioPath: String?, pendingResult: PendingResult) {
        if (audioPath == null) {
            finishWork(pendingResult)
            return
        }

        val isTest = intent.getBooleanExtra("FORCE_TEST", false)

        if (!isTest) {
            val daysString = intent.getStringExtra("DAYS") ?: ""
            val isPermanent = intent.getBooleanExtra("IS_PERMANENT", true)
            val creationTime = intent.getLongExtra("CREATION_TIME", 0L)

            val startH = intent.getIntExtra("START_H", 0)
            val startM = intent.getIntExtra("START_M", 0)
            var endH = intent.getIntExtra("END_H", 23)
            val endM = intent.getIntExtra("END_M", 59)

            if (endH == 0) endH = 24

            val now = Calendar.getInstance()

            val today = now.get(Calendar.DAY_OF_WEEK)
            val selectedDays = daysString.split(",").mapNotNull { it.toIntOrNull() }
            if (selectedDays.isNotEmpty() && today !in selectedDays) {
                finishWork(pendingResult)
                return
            }

            val currentMinuteOfDay = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            val startMinuteOfDay = startH * 60 + startM
            val endMinuteOfDay = endH * 60 + endM

            if (currentMinuteOfDay < startMinuteOfDay || currentMinuteOfDay > endMinuteOfDay) {
                finishWork(pendingResult)
                return
            }

            if (!isPermanent) {
                val oneWeekInMillis = 7 * 24 * 60 * 60 * 1000L
                if (System.currentTimeMillis() - creationTime > oneWeekInMillis) {
                    finishWork(pendingResult)
                    return
                }
            }
        } else {
            Toast.makeText(context, "🔔 PRUEBA DE SONIDO", Toast.LENGTH_SHORT).show()
        }

        playReminderAudio(context, audioPath, pendingResult)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun playReminderAudio(context: Context, path: String, pendingResult: PendingResult) {
        val file = File(path)
        if (!file.exists()) {
            finishWork(pendingResult)
            return
        }

        try {
            stopCurrentAudio()

            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val useSpeaker = PreferenceHelper.isSpeakerEnabled(context)

            // MAXIMIZAR VOLUMEN
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)

            currentMediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(context, Uri.fromFile(file))
                setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                prepare()

                // RUTEO DE AUDIO INTELIGENTE (CORREGIDO)
                if (useSpeaker && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    // AQUÍ ESTABA EL ERROR: Es GET_DEVICES_OUTPUTS (Plural)
                    val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                    val speaker = devices.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                    if (speaker != null) {
                        // AQUÍ TAMBIÉN: Usamos el método setPreferredDevice
                        setPreferredDevice(speaker)
                    }
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        setPreferredDevice(null) // Dejar que el sistema elija (BT/Auriculares)
                    }
                }
            }

            // AMPLIFICADOR DE SONIDO (+20dB)
            try {
                val sessionId = currentMediaPlayer!!.audioSessionId
                loudnessEnhancer = LoudnessEnhancer(sessionId)
                loudnessEnhancer?.setTargetGain(2000)
                loudnessEnhancer?.enabled = true
            } catch (e: Exception) { e.printStackTrace() }

            var timesPlayed = 0
            currentMediaPlayer?.setOnCompletionListener { mp ->
                timesPlayed++
                if (timesPlayed < 2) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        try { if (currentMediaPlayer == mp) mp.start() } catch (e: Exception) { cleanupAndFinish(pendingResult) }
                    }, 2000)
                } else {
                    cleanupAndFinish(pendingResult)
                }
            }

            currentMediaPlayer?.setOnErrorListener { _, _, _ ->
                cleanupAndFinish(pendingResult)
                true
            }

            currentMediaPlayer?.start()

        } catch (e: Exception) {
            e.printStackTrace()
            cleanupAndFinish(pendingResult)
        }
    }

    private fun cleanupAndFinish(pendingResult: PendingResult) {
        finishWork(pendingResult)
    }

    private fun finishWork(pendingResult: PendingResult) {
        if (wakeLock?.isHeld == true) {
            try { wakeLock?.release() } catch (e: Exception) {}
        }
        pendingResult.finish()
    }
}