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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.recordatoriosdepuchi.utils.PreferenceHelper

@Composable
fun AdminSettingsTab() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estados existentes
    var isSpeakerEnabled by remember { mutableStateOf(PreferenceHelper.isSpeakerEnabled(context)) }
    var screensaverTimeout by remember { mutableFloatStateOf(PreferenceHelper.getScreensaverTimeout(context).toFloat()) }
    var isPuppetEnabled by remember { mutableStateOf(PreferenceHelper.isPuppetEnabled(context)) }
    var puppetInterval by remember { mutableFloatStateOf(PreferenceHelper.getPuppetInterval(context).toFloat()) }

    // NUEVOS ESTADOS DE VOLUMEN
    var ringVol by remember { mutableFloatStateOf(PreferenceHelper.getRingVolume(context)) }
    var voiceVol by remember { mutableFloatStateOf(PreferenceHelper.getVoiceVolume(context)) }
    var effectsVol by remember { mutableFloatStateOf(PreferenceHelper.getEffectsVolume(context)) }

    // NUEVO ESTADO MÚSICA FONDO
    var musicVol by remember { mutableFloatStateOf(PreferenceHelper.getBackgroundMusicVolume(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // SECCIÓN 1: GENERAL
        SettingsCard(title = "Hardware y Llamadas", icon = Icons.Default.Settings) {
            SwitchRow(
                label = "Altavoz Automático",
                description = "Activa el manos libres al llamar",
                checked = isSpeakerEnabled,
                onCheckedChange = {
                    isSpeakerEnabled = it
                    PreferenceHelper.setSpeakerEnabled(context, it)
                }
            )
        }

        // SECCIÓN 2: CONTROL DE AUDIO (LA NUEVA)
        SettingsCard(title = "Mezclador de Audio", icon = Icons.Default.VolumeUp) {
            VolumeSliderRow(
                label = "Volumen Tono Llamada",
                value = ringVol,
                onValueChange = {
                    ringVol = it
                    PreferenceHelper.setRingVolume(context, it)
                }
            )
            Divider()
            VolumeSliderRow(
                label = "Volumen Voz Asistente",
                value = voiceVol,
                onValueChange = {
                    voiceVol = it
                    PreferenceHelper.setVoiceVolume(context, it)
                }
            )
            Divider()
            VolumeSliderRow(
                label = "Volumen Efectos Sonido",
                value = effectsVol,
                onValueChange = {
                    effectsVol = it
                    PreferenceHelper.setEffectsVolume(context, it)
                }
            )
            Divider()
            VolumeSliderRow(
                label = "Música de Fondo en Salvapantallas",
                value = musicVol,
                onValueChange = {
                    musicVol = it
                    PreferenceHelper.setBackgroundMusicVolume(context, it)
                }
            )
        }

        // SECCIÓN 3: ASISTENTE
        SettingsCard(title = "Inactividad y Asistente", icon = Icons.Default.Face) {
            SliderRow(
                label = "Tiempo para salvapantallas",
                value = screensaverTimeout,
                valueRange = 10f..120f,
                steps = 10,
                suffix = "seg",
                onValueChange = {
                    screensaverTimeout = it
                    PreferenceHelper.setScreensaverTimeout(context, it.toInt())
                }
            )

            Divider()

            SwitchRow(
                label = "Activar Asistente",
                description = "Un asistente saldrá para decirle que puede hacer con este dispositivo",
                checked = isPuppetEnabled,
                onCheckedChange = {
                    isPuppetEnabled = it
                    PreferenceHelper.setPuppetEnabled(context, it)
                }
            )

            if (isPuppetEnabled) {
                SliderRow(
                    label = "Frecuencia de Aparición",
                    value = puppetInterval,
                    valueRange = 5f..240f,
                    steps = 46,
                    suffix = "min",
                    onValueChange = {
                        puppetInterval = it
                        PreferenceHelper.setPuppetInterval(context, it.toInt())
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun SwitchRow(label: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SliderRow(label: String, value: Float, valueRange: ClosedFloatingPointRange<Float>, steps: Int, suffix: String, onValueChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${value.toInt()} $suffix", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}

@Composable
fun VolumeSliderRow(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${(value * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
        // MODIFICADO: steps = 0 para que sea continuo y muy preciso
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            steps = 0
        )
    }
}