package com.example.recordatoriosdepuchi.ui

import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.recordatoriosdepuchi.service.PuchiCallService
import com.example.recordatoriosdepuchi.utils.CallContext
import kotlinx.coroutines.delay
import java.io.File

class CallActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Comprobación de seguridad: Si llegamos aquí y la llamada ya murió, nos vamos.
        if (PuchiCallService.currentCall == null || PuchiCallService.currentCall?.state == Call.STATE_DISCONNECTED) {
            finish()
            return
        }

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            val contact = CallContext.currentContact

            CallScreenUI(
                name = contact?.name ?: "Desconocido",
                photoUri = contact?.photoUri,
                onHangUp = {
                    PuchiCallService.currentCall?.disconnect()
                    finishAndRemoveTask() // Cierra más agresivamente
                }
            )
        }
    }
}

@Composable
fun CallScreenUI(name: String, photoUri: String?, onHangUp: () -> Unit) {
    // MONITOR DE ESTADO: Si la llamada se corta (por falta de SIM), cerramos YA.
    LaunchedEffect(Unit) {
        while (true) {
            val call = PuchiCallService.currentCall
            if (call == null || call.state == Call.STATE_DISCONNECTED || call.state == Call.STATE_DISCONNECTING) {
                onHangUp()
                break
            }
            delay(500)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // FOTO
        if (!photoUri.isNullOrEmpty()) {
            AsyncImage(
                model = File(photoUri),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFF263238)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(200.dp))
            }
        }

        // DEGRADADO OSCURO
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        // CONTENIDO
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 60.dp)) {
                Text("LLAMANDO A...", color = Color.Green, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = name.uppercase(),
                    color = Color.White,
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 55.sp
                )
            }

            // BOTÓN COLGAR GIGANTE
            Button(
                onClick = onHangUp,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)), // Rojo Brillante
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(bottom = 30.dp)
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(16.dp))
                Text("COLGAR", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
    }
}