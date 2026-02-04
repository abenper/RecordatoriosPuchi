package com.example.recordatoriosdepuchi.ui.components.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recordatoriosdepuchi.utils.PreferenceHelper

@Composable
fun AdminSettingsTab() {
    val context = LocalContext.current
    var isSpeaker by remember { mutableStateOf(PreferenceHelper.isSpeakerEnabled(context)) }
    var timeoutSecs by remember { mutableFloatStateOf(PreferenceHelper.getScreensaverTimeout(context).toFloat()) }
    var puppetIntervalMins by remember { mutableFloatStateOf(PreferenceHelper.getPuppetInterval(context).toFloat()) }
    var showPuppet by remember { mutableStateOf(PreferenceHelper.isPuppetEnabled(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Configuración", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF455A64))
        Spacer(modifier = Modifier.height(24.dp))

        // 1. AUDIO
        SettingsCard(title = "Audio Llamadas", icon = Icons.Default.VolumeUp) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(if (isSpeaker) "Modo: Altavoz (Manos Libres)" else "Modo: Auricular (Privado)", modifier = Modifier.weight(1f))
                Switch(checked = isSpeaker, onCheckedChange = {
                    isSpeaker = it
                    PreferenceHelper.setSpeakerEnabled(context, it)
                })
            }
        }

        // 2. SALVAPANTALLAS
        SettingsCard(title = "Tiempo Inactividad", icon = Icons.Default.Timer) {
            Text("${timeoutSecs.toInt()} segundos", fontWeight = FontWeight.Bold)
            Slider(
                value = timeoutSecs,
                onValueChange = { timeoutSecs = it },
                onValueChangeFinished = { PreferenceHelper.setScreensaverTimeout(context, timeoutSecs.toInt()) },
                valueRange = 30f..300f, // 30s a 5 min
                steps = 8
            )
            Text("Tiempo antes de que salga el protector de pantalla", fontSize = 12.sp, color = Color.Gray)
        }

        // 3. EL MUÑECO
        SettingsCard(title = "Asistente Virtual (Muñeco)", icon = Icons.Default.SmartToy) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Activar Muñeco", modifier = Modifier.weight(1f))
                Switch(checked = showPuppet, onCheckedChange = {
                    showPuppet = it
                    PreferenceHelper.setPuppetEnabled(context, it)
                })
            }

            if (showPuppet) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Aparecer cada: ${puppetIntervalMins.toInt()} minutos")
                Slider(
                    value = puppetIntervalMins,
                    onValueChange = { puppetIntervalMins = it },
                    onValueChangeFinished = { PreferenceHelper.setPuppetInterval(context, puppetIntervalMins.toInt()) },
                    valueRange = 5f..120f,
                    steps = 22
                )
            }
        }
    }
}

@Composable
fun SettingsCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            content()
        }
    }
}