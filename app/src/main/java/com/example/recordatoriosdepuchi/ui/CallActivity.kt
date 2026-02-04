package com.example.recordatoriosdepuchi.ui

import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File

class CallActivity : ComponentActivity() {

    private var audioManager: AudioManager? = null
    // Evita que se intente navegar dos veces a la vez (error común)
    private var isNavigating = false

    // Callback del sistema: salta cuando cambia el estado de la llamada
    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            if (state == Call.STATE_DISCONNECTED || state == Call.STATE_DISCONNECTING) {
                navigateToHome()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PuchiCallService.currentCall?.registerCallback(callCallback)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { }
        })

        // Seguridad: Si entras y no hay llamada, fuera.
        if (PuchiCallService.currentCall == null || PuchiCallService.currentCall?.state == Call.STATE_DISCONNECTED) {
            navigateToHome()
            return
        }

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        configureCallAudio()

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            val contact = CallContext.currentContact

            // --- VIGILANTE DE LA INTERFAZ ---
            // Si el callback falla, esto revisa cada 0.5s si la llamada sigue viva.
            // Si no hay llamada, te manda al menú.
            LaunchedEffect(Unit) {
                while (isActive) {
                    val call = PuchiCallService.currentCall
                    if (call == null || call.state == Call.STATE_DISCONNECTED || call.state == Call.STATE_DISCONNECTING) {
                        navigateToHome()
                        break
                    }
                    delay(500)
                }
            }

            var seconds by remember { mutableLongStateOf(0L) }
            LaunchedEffect(Unit) {
                val startTime = System.currentTimeMillis()
                while (isActive) {
                    seconds = (System.currentTimeMillis() - startTime) / 1000
                    delay(1000)
                }
            }
            val timeString = String.format("%02d:%02d", seconds / 60, seconds % 60)

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
                    Box(
                        modifier = Modifier.size(320.dp).clip(CircleShape).background(Color.Gray)
                    ) {
                        if (contact?.photoUri?.isNotEmpty() == true) {
                            AsyncImage(
                                model = File(contact.photoUri),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(
                        text = (contact?.name ?: "Desconocido").uppercase(),
                        color = Color.White,
                        fontSize = 55.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 60.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = timeString,
                        color = Color(0xFF00E676),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            PuchiCallService.currentCall?.disconnect()
                            navigateToHome()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                        modifier = Modifier.fillMaxWidth(0.95f).height(110.dp),
                        shape = RoundedCornerShape(100),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CallEnd, "Colgar", Modifier.size(55.dp), Color.White)
                            Spacer(Modifier.width(25.dp))
                            Text("COLGAR", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }

    private fun configureCallAudio() {
        try {
            val maxVoice = audioManager?.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) ?: 5
            audioManager?.setStreamVolume(AudioManager.STREAM_VOICE_CALL, maxVoice, 0)

            val devices = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS) ?: emptyArray()
            val isBluetoothConnected = devices.any {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
            }

            if (isBluetoothConnected) {
                audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
                audioManager?.startBluetoothSco()
                audioManager?.isBluetoothScoOn = true
                audioManager?.isSpeakerphoneOn = false
            } else {
                audioManager?.mode = AudioManager.MODE_IN_CALL
                audioManager?.isSpeakerphoneOn = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- CORRECCIÓN PANTALLA BLANCA ---
    private fun navigateToHome() {
        if (isNavigating) return
        isNavigating = true

        // Forzamos la ejecución en el hilo principal para que la UI responda
        runOnUiThread {
            try {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Usamos finish() en lugar de finishAndRemoveTask().
            // finishAndRemoveTask() es lo que estaba matando la app y dejando la pantalla blanca.
            finish()
        }
    }

    override fun onDestroy() {
        PuchiCallService.currentCall?.unregisterCallback(callCallback)
        if (audioManager?.isBluetoothScoOn == true) {
            audioManager?.stopBluetoothSco()
            audioManager?.isBluetoothScoOn = false
        }
        super.onDestroy()
    }
}