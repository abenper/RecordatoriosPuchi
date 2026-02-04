package com.example.recordatoriosdepuchi.ui

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.telecom.Call
import android.telecom.VideoProfile
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.recordatoriosdepuchi.MainActivity
import com.example.recordatoriosdepuchi.service.PuchiCallService
import com.example.recordatoriosdepuchi.utils.CallContext
import com.example.recordatoriosdepuchi.utils.PreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class IncomingCallActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var mediaPlayer: MediaPlayer? = null
    private var textToSpeech: TextToSpeech? = null
    private var contactName: String = "Familia"
    private var ttsJob: Job? = null

    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null
    private var originalMode: Int = AudioManager.MODE_NORMAL

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            if (state == Call.STATE_DISCONNECTED) {
                navigateToHome()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as android.app.KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)

        PuchiCallService.currentCall?.registerCallback(callCallback)

        val contact = CallContext.currentContact
        contactName = contact?.name ?: "Familia"

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        originalMode = audioManager?.mode ?: AudioManager.MODE_NORMAL

        routeAudioToBestOutput()
        requestAudioFocus()

        textToSpeech = TextToSpeech(this, this)
        startRingtone()

        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(320.dp).clip(CircleShape).background(Color.Gray)) {
                        if (contact?.photoUri?.isNotEmpty() == true) {
                            AsyncImage(
                                model = File(contact.photoUri),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        text = contactName.uppercase(),
                        color = Color.White,
                        fontSize = 55.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 60.sp
                    )
                }

                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 50.dp), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = { answerCall() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        modifier = Modifier.fillMaxWidth(0.95f).height(110.dp),
                        shape = RoundedCornerShape(100),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Call, null, Modifier.size(55.dp), Color.White)
                            Spacer(Modifier.width(25.dp))
                            Text("DESCOLGAR", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }

    private fun routeAudioToBestOutput() {
        // --- CORRECCIÓN AQUÍ TAMBIÉN ---
        val devices = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS) ?: emptyArray()
        val isBluetoothConnected = devices.any {
            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
        }

        if (isBluetoothConnected) {
            audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager?.startBluetoothSco()
            audioManager?.isBluetoothScoOn = true
        } else {
            audioManager?.mode = AudioManager.MODE_RINGTONE
            audioManager?.isSpeakerphoneOn = true
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { }
                .build()
            audioManager?.requestAudioFocus(focusRequest!!)
        }
    }

    private fun startRingtone() {
        try {
            val defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)
            val ringVolume = PreferenceHelper.getRingVolume(this)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, defaultRingtoneUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                setVolume(ringVolume, ringVolume)
                prepare()
                start()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale("es", "ES"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech?.setLanguage(Locale("es"))
            }
            textToSpeech?.setSpeechRate(0.9f)

            val audioAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            textToSpeech?.setAudioAttributes(audioAttrs)

            startSpeakingLoop()
        }
    }

    private fun startSpeakingLoop() {
        ttsJob = CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            while (true) {
                speakMessage()
                delay(4000)
            }
        }
    }

    private fun speakMessage() {
        val text = "Puchi, llamada de $contactName"
        val params = Bundle()
        val voiceVolume = PreferenceHelper.getVoiceVolume(this)
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, voiceVolume)
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "ID_MSG")
    }

    private fun answerCall() {
        PuchiCallService.currentCall?.answer(VideoProfile.STATE_AUDIO_ONLY)
        stopMedia()
        val intent = Intent(this, CallActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun navigateToHome() {
        try {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        } catch (e: Exception) { e.printStackTrace() }
        finishAndRemoveTask()
    }

    private fun stopMedia() {
        try {
            ttsJob?.cancel()
            if (mediaPlayer?.isPlaying == true) mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && focusRequest != null) {
                audioManager?.abandonAudioFocusRequest(focusRequest!!)
            }
            audioManager?.mode = originalMode
            if (audioManager?.isBluetoothScoOn == true) {
                audioManager?.stopBluetoothSco()
                audioManager?.isBluetoothScoOn = false
            }

        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onDestroy() {
        stopMedia()
        PuchiCallService.currentCall?.unregisterCallback(callCallback)
        super.onDestroy()
    }
}