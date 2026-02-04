package com.example.recordatoriosdepuchi.ui

import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.telecom.VideoProfile
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.recordatoriosdepuchi.service.PuchiCallService
import com.example.recordatoriosdepuchi.utils.CallContext
import java.io.File

class IncomingCallActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as android.app.KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)

        setContent {
            val contact = CallContext.currentContact
            Column(
                modifier = Modifier.fillMaxSize().background(Color.Black).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 40.dp)) {
                    Text("TE ESTÁ LLAMANDO:", color = Color.White, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(modifier = Modifier.size(280.dp).clip(CircleShape).background(Color.Gray)) {
                        if (contact?.photoUri?.isNotEmpty() == true) {
                            AsyncImage(model = File(contact.photoUri), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(contact?.name ?: "Familia", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 60.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = { PuchiCallService.currentCall?.reject(false, null); finish() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.size(100.dp), shape = CircleShape
                    ) { Icon(Icons.Default.CallEnd, null, modifier = Modifier.size(40.dp)) }

                    Button(
                        onClick = { PuchiCallService.currentCall?.answer(VideoProfile.STATE_AUDIO_ONLY); finish() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        modifier = Modifier.size(120.dp), shape = CircleShape
                    ) { Icon(Icons.Default.Call, null, modifier = Modifier.size(50.dp)) }
                }
            }
        }
    }
}