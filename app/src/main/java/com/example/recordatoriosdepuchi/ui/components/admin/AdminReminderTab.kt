package com.example.recordatoriosdepuchi.ui.components.admin

import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.recordatoriosdepuchi.data.local.entity.ReminderEntity
import com.example.recordatoriosdepuchi.ui.viewmodel.HomeViewModel
import com.example.recordatoriosdepuchi.utils.AudioRecorder
import com.example.recordatoriosdepuchi.utils.ReminderScheduler
import kotlinx.coroutines.delay
import java.io.File
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Composable
fun AdminReminderTab(viewModel: HomeViewModel) {
    val reminders by viewModel.reminders.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var reminderToEdit by remember { mutableStateOf<ReminderEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- CABECERA ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Avisos de Voz",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text("Mensajes programados", color = Color.Gray, fontSize = 14.sp)
            }

            // BOTÓN AÑADIR
            Button(
                onClick = {
                    reminderToEdit = null
                    showDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("NUEVO")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(reminders) { reminder ->
                ReminderCard(
                    reminder,
                    onEdit = {
                        reminderToEdit = reminder
                        showDialog = true
                    },
                    onDelete = { viewModel.deleteReminder(reminder) },
                    onTest = { viewModel.testReminder(reminder) }
                )
            }
        }
    }

    if (showDialog) {
        ReminderFullScreenDialog(
            reminderToEdit = reminderToEdit,
            onDismiss = { showDialog = false },
            onSave = { name, path, startH, startM, endH, endM, interval, permanent, daysOfWeek ->
                if (reminderToEdit == null) {
                    viewModel.addAdvancedReminder(name, path, startH, startM, endH, endM, interval, permanent, daysOfWeek)
                } else {
                    val updatedReminder = reminderToEdit!!.copy(
                        name = name,
                        audioPath = path,
                        startHour = startH,
                        startMinute = startM,
                        endHour = endH,
                        endMinute = endM,
                        intervalMinutes = interval,
                        isPermanent = permanent,
                        daysOfWeek = daysOfWeek.joinToString(",")
                    )
                    viewModel.updateReminder(updatedReminder)
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun ReminderCard(
    reminder: ReminderEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit
) {
    val context = LocalContext.current
    val scheduler = remember { ReminderScheduler(context) }

    // 1. ESTADO PARA EL TIEMPO ACTUAL
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // 2. DISPARADOR DE ACTUALIZACIÓN (Para forzar el recálculo cuando llegue a 0)
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // 3. CÁLCULO DINÁMICO (Se recalcula si cambia el recordatorio O el disparador)
    val nextTime = remember(reminder, refreshTrigger) {
        scheduler.getNextTriggerTime(reminder)
    }

    // Bucle de reloj: Actualiza cada segundo y verifica si ya llegamos a la hora
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = System.currentTimeMillis()

            // SI YA PASÓ LA HORA PREVISTA: Forzamos recálculo inmediato
            if (nextTime != null && currentTime >= nextTime) {
                refreshTrigger++ // Esto hace que 'nextTime' se vuelva a calcular para el siguiente intervalo
            }

            delay(1000)
        }
    }

    val timeInfo = if (nextTime != null) {
        val diff = nextTime - currentTime
        if (diff > 0) {
            val h = TimeUnit.MILLISECONDS.toHours(diff)
            val m = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
            val s = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
            if (h > 0) "Próxima: ${h}h ${m}m" else "Próxima: ${m}m ${s}s"
        } else {
            "Reproduciendo..." // Mensaje temporal justo en el cambio
        }
    } else {
        "Inactivo hoy"
    }

    Card(
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
        shape = RoundedCornerShape(16.dp),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(reminder.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                // Muestra la cuenta atrás VIVA
                Text(timeInfo, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${String.format("%02d:%02d", reminder.startHour, reminder.startMinute)} - ${String.format("%02d:%02d", reminder.endHour, reminder.endMinute)}",
                        fontSize = 14.sp, color = Color.DarkGray
                    )
                }
                Text("Cada ${reminder.intervalMinutes} min", fontSize = 12.sp, color = Color.Blue)
            }

            Row {
                IconButton(onClick = onTest) {
                    Icon(Icons.Default.PlayArrow, "Probar", tint = Color(0xFF2E7D32))
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red)
                }
            }
        }
    }
}

// ... (ReminderFullScreenDialog SE MANTIENE IGUAL) ...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderFullScreenDialog(
    reminderToEdit: ReminderEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Int, Int, Int, Int, Boolean, List<Int>) -> Unit
) {
    var name by remember(reminderToEdit) { mutableStateOf(reminderToEdit?.name ?: "") }
    var startHour by remember(reminderToEdit) { mutableStateOf(reminderToEdit?.startHour?.toString() ?: "10") }
    var startMin by remember(reminderToEdit) { mutableStateOf(reminderToEdit?.startMinute?.toString() ?: "00") }
    var endHour by remember(reminderToEdit) { mutableStateOf(reminderToEdit?.endHour?.toString() ?: "22") }
    var endMin by remember(reminderToEdit) { mutableStateOf(reminderToEdit?.endMinute?.toString() ?: "00") }
    var interval by remember(reminderToEdit) { mutableStateOf(reminderToEdit?.intervalMinutes?.toString() ?: "60") }
    var isPermanent by remember(reminderToEdit) { mutableStateOf(reminderToEdit?.isPermanent ?: true) }

    // VALIDACIÓN 24h INICIAL
    LaunchedEffect(endHour) {
        if (endHour == "24") endMin = "00"
    }

    val daysOfWeekMap = listOf(
        "L" to Calendar.MONDAY, "M" to Calendar.TUESDAY, "X" to Calendar.WEDNESDAY,
        "J" to Calendar.THURSDAY, "V" to Calendar.FRIDAY, "S" to Calendar.SATURDAY, "D" to Calendar.SUNDAY
    )

    val initialSelectedDays = remember(reminderToEdit) {
        if (reminderToEdit != null && reminderToEdit.daysOfWeek.isNotEmpty()) {
            reminderToEdit.daysOfWeek.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        } else {
            daysOfWeekMap.map { it.second }.toSet()
        }
    }
    var selectedDays by remember(reminderToEdit) { mutableStateOf(initialSelectedDays) }
    var recordedAudioPath by remember(reminderToEdit) { mutableStateOf<String?>(reminderToEdit?.audioPath) }
    var isRecording by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val audioRecorder = remember { AudioRecorder(context) }
    val scale by animateFloatAsState(if (isRecording) 1.5f else 1f, label = "recScale")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (reminderToEdit == null) "Nuevo Aviso" else "Editar Aviso") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Cerrar") }
                    },
                    actions = {
                        TextButton(onClick = {
                            if (name.isNotEmpty() && recordedAudioPath != null && interval.isNotEmpty() && selectedDays.isNotEmpty()) {
                                onSave(
                                    name, recordedAudioPath!!,
                                    startHour.toIntOrNull() ?: 10, startMin.toIntOrNull() ?: 0,
                                    endHour.toIntOrNull() ?: 22, endMin.toIntOrNull() ?: 0,
                                    interval.toIntOrNull() ?: 60,
                                    isPermanent,
                                    selectedDays.toList()
                                )
                            } else {
                                Toast.makeText(context, "Faltan datos", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("GUARDAR", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))
                Text("Horario de actividad:", fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = startHour, onValueChange = { startHour = it }, label = { Text("H.Inicio") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = startMin, onValueChange = { startMin = it }, label = { Text("Min") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("- hasta -", fontSize = 12.sp, color = Color.Gray)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = endHour,
                        onValueChange = {
                            endHour = it
                            if (it == "24") endMin = "00"
                        },
                        label = { Text("H.Fin") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = endMin,
                        onValueChange = { endMin = it },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = endHour != "24"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Días de la semana:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    daysOfWeekMap.forEach { (label, dayConst) ->
                        val isSelected = dayConst in selectedDays
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray)
                                .clickable { selectedDays = if (isSelected) selectedDays - dayConst else selectedDays + dayConst },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = if (isSelected) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Repetir cada (min):", modifier = Modifier.weight(1f))
                    OutlinedTextField(value = interval, onValueChange = { interval = it }, modifier = Modifier.width(100.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Duración:", modifier = Modifier.weight(1f))
                    FilterChip(selected = !isPermanent, onClick = { isPermanent = false }, label = { Text("Semana") })
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(selected = isPermanent, onClick = { isPermanent = true }, label = { Text("Siempre") })
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                Text("Mensaje de voz:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(if (isRecording) Color.Red else MaterialTheme.colorScheme.tertiary)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        try {
                                            isRecording = true
                                            val fileName = "voice_msg_${System.currentTimeMillis()}"
                                            val path = audioRecorder.startRecording(fileName)
                                            tryAwaitRelease()
                                            audioRecorder.stopRecording()
                                            isRecording = false
                                            if (path != null) recordedAudioPath = path
                                        } catch (e: Exception) {
                                            isRecording = false
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Mic, null, tint = Color.White, modifier = Modifier.size(50.dp))
                    }
                }

                Text(
                    if (isRecording) "GRABANDO..." else if (recordedAudioPath != null) "Audio Guardado ✓" else "MANTÉN PARA GRABAR",
                    color = if (isRecording) Color.Red else Color.Gray,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                if (recordedAudioPath != null && !isRecording) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            try {
                                val mp = MediaPlayer.create(context, Uri.fromFile(File(recordedAudioPath!!)))
                                mp.start()
                            } catch (e: Exception) { Toast.makeText(context, "Error audio", Toast.LENGTH_SHORT).show() }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Text("Escuchar")
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}