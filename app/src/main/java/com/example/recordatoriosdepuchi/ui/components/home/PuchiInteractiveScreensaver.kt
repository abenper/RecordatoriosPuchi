package com.example.recordatoriosdepuchi.ui.components.home

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.recordatoriosdepuchi.R
import com.example.recordatoriosdepuchi.data.local.entity.ContactEntity
import com.example.recordatoriosdepuchi.utils.PreferenceHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.math.sqrt

// Enum para controlar la máquina de estados de la animación
enum class AssistantScene {
    HIDDEN, // Pantalla de burbujas (Salvapantallas pasivo)
    FACE,   // Cara del asistente hablando (Interacción activa)
    HAND    // Animación de saludo (Refuerzo visual)
}

/**
 * Componente principal del Asistente Virtual "Puchi".
 * * Funcionalidad:
 * 1. Actúa como salvapantallas interactivo para evitar el quemado de pantalla OLED.
 * 2. Proporciona compañía mediante un avatar animado que habla y saluda.
 * 3. Muestra "burbujas" flotantes con los contactos para incitar a la comunicación.
 * 4. Detecta movimiento físico (acelerómetro) para "despertar" la app.
 */
@Composable
fun PuchiInteractiveScreensaver(
    contacts: List<ContactEntity>,
    testMode: Boolean = false,
    onWakeUp: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // CONFIGURACIÓN PERSONALIZADA
    val puppetIntervalMinutes = remember { PreferenceHelper.getPuppetInterval(context) }
    val isPuppetEnabled = remember { PreferenceHelper.isPuppetEnabled(context) }

    var isNightMode by remember { mutableStateOf(false) }
    var currentContact by remember { mutableStateOf<ContactEntity?>(null) }
    var isBubbleVisible by remember { mutableStateOf(false) }
    var currentScene by remember { mutableStateOf(AssistantScene.HIDDEN) }
    var isSpeaking by remember { mutableStateOf(false) }

    // --- SENSOR DE MOVIMIENTO (ACELERÓMETRO) ---
    // Detecta si la abuela coge el teléfono para desactivar el salvapantallas automáticamente.
    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            private var currentAcceleration = SensorManager.GRAVITY_EARTH
            private var lastAcceleration = SensorManager.GRAVITY_EARTH
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val x = it.values[0]; val y = it.values[1]; val z = it.values[2]
                    lastAcceleration = currentAcceleration
                    currentAcceleration = sqrt((x*x + y*y + z*z).toDouble()).toFloat()
                    val delta = currentAcceleration - lastAcceleration
                    // Umbral de sensibilidad para evitar falsos positivos
                    if (kotlin.math.abs(delta) > 1.2f) { onWakeUp() }
                }
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // --- ANIMACIONES COMPOSE ---
    // Usamos Transition API para animaciones fluidas y eficientes.

    // 1. Parpadeo de ojos (Naturalidad)
    val blinkAnim = rememberInfiniteTransition(label = "blink")
    val eyeScaleY by blinkAnim.animateFloat(
        initialValue = 1f, targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, delayMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "eyeScale"
    )

    // 2. Sincronización labial simulada (Lipsync)
    val mouthAnim = rememberInfiniteTransition(label = "mouth")
    val targetMouth = if (isSpeaking) 0.6f else 0.2f
    val initialMouth = if (isSpeaking) 0.3f else 0.2f

    val mouthHeight by mouthAnim.animateFloat(
        initialValue = initialMouth, targetValue = targetMouth,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 400
                0.3f at 0
                0.6f at 200
                0.3f at 400
            },
            repeatMode = RepeatMode.Reverse
        ), label = "mouthHeight"
    )

    // 3. Animación de mano saludando (Rotación pendular)
    val handAnim = rememberInfiniteTransition(label = "handWave")
    val handRotation by handAnim.animateFloat(
        initialValue = -15f, targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "handRot"
    )

    // 4. Flotación de burbujas (Evita quemado de pantalla OLED moviendo pixels)
    val floatAnim = rememberInfiniteTransition(label = "float")
    val offsetY by floatAnim.animateFloat(
        initialValue = 15f, targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "offsetY"
    )

    // --- LÓGICA DE FONDO (BURBUJAS) ---
    // Ciclo infinito que muestra contactos aleatorios flotando para sugerir llamadas.
    LaunchedEffect(contacts, testMode) {
        var contactIndex = 0
        while (isActive) {
            val cal = Calendar.getInstance()
            val minOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            // Modo noche: No molestar entre las 22:30 y las 09:30
            isNightMode = (minOfDay >= 1350) || (minOfDay < 570)

            if (!isNightMode || testMode) {
                if (currentScene == AssistantScene.HIDDEN && contacts.isNotEmpty()) {
                    currentContact = contacts[contactIndex]
                    contactIndex = (contactIndex + 1) % contacts.size
                    isBubbleVisible = true
                    delay(8000) // Mostrar 8 segundos
                    isBubbleVisible = false
                    delay(3000) // Esperar 3 segundos
                } else {
                    delay(1000)
                }
            } else {
                isBubbleVisible = false
                currentScene = AssistantScene.HIDDEN
                delay(60000)
            }
        }
    }

    // --- GUION DEL ASISTENTE (COREOGRAFÍA) ---
    LaunchedEffect(testMode) {
        var isFirstLoop = true

        while(isActive) {
            val shouldRun = testMode || (!isNightMode && isPuppetEnabled)

            if (shouldRun) {
                val intervalMillis = puppetIntervalMinutes * 60 * 1000L
                val waitTime = if (testMode && isFirstLoop) 1000L else intervalMillis

                delay(waitTime)

                // ACTO 1: Aparición
                currentScene = AssistantScene.FACE
                playAudio(context, R.raw.entrada, volume = 0.3f)

                // ACTO 2: Saludo Verbal
                isSpeaking = true
                playAudio(context, R.raw.hola, volume = 1.0f)
                isSpeaking = false

                // ACTO 3: Saludo Visual (Mano)
                delay(100)
                currentScene = AssistantScene.HAND
                playAudio(context, R.raw.agitarmano, volume = 0.3f)

                // ACTO 4: Explicación y cierre
                currentScene = AssistantScene.FACE
                delay(300)
                isSpeaking = true
                playAudio(context, R.raw.telefonointeligente, volume = 1.0f)
                isSpeaking = false

                // FINAL
                delay(2000)
                currentScene = AssistantScene.HIDDEN

                isFirstLoop = false
            } else {
                delay(60000)
            }
        }
    }

    // --- UI (Layout) ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                onWakeUp()
            },
        contentAlignment = Alignment.Center
    ) {
        // CAPA 1: BURBUJAS FLOTANTES
        AnimatedVisibility(
            visible = isBubbleVisible && currentScene == AssistantScene.HIDDEN,
            enter = fadeIn(tween(2000)),
            exit = fadeOut(tween(2000))
        ) {
            currentContact?.let { contact ->
                Column(
                    modifier = Modifier
                        .offset(y = with(density) { offsetY.toDp() })
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "LLAMAR A:",
                        fontSize = 32.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(modifier = Modifier.size(320.dp).clip(CircleShape).background(Color.DarkGray)) {
                        if (contact.photoUri.isNotEmpty()) {
                            AsyncImage(model = File(contact.photoUri), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(contact.name.take(1), fontSize = 120.sp, color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(contact.name, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center)
                }
            }
        }

        // CAPA 2: EL ASISTENTE (Transición de escenas)
        AnimatedContent(
            targetState = currentScene,
            transitionSpec = {
                (fadeIn(animationSpec = tween(500)) + scaleIn()).togetherWith(fadeOut(animationSpec = tween(500)) + scaleOut())
            },
            label = "SceneTransition"
        ) { scene ->
            when (scene) {
                AssistantScene.HIDDEN -> Box(Modifier.fillMaxSize())
                AssistantScene.FACE -> {
                    // Renderizado de la cara (Círculos y formas geométricas simples)
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1565C0)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                                // Ojos con animación de parpadeo
                                Box(modifier = Modifier.size(100.dp).scale(1f, eyeScaleY).clip(CircleShape).background(Color.White)) {
                                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Black).align(Alignment.Center))
                                }
                                Box(modifier = Modifier.size(100.dp).scale(1f, eyeScaleY).clip(CircleShape).background(Color.White)) {
                                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Black).align(Alignment.Center))
                                }
                            }
                            Spacer(modifier = Modifier.height(80.dp))
                            // Boca con animación de altura
                            Box(modifier = Modifier.height(120.dp), contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.width(120.dp).height(80.dp * mouthHeight).clip(RoundedCornerShape(40.dp)).background(Color.White))
                            }
                        }
                    }
                }
                AssistantScene.HAND -> {
                    // Renderizado de la mano saludando
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1565C0)), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.PanTool,
                            contentDescription = "Saludo",
                            tint = Color.White,
                            modifier = Modifier.size(300.dp).graphicsLayer {
                                rotationZ = handRotation
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Función auxiliar para reproducir audio de forma asíncrona
suspend fun playAudio(context: Context, resId: Int, volume: Float = 1.0f) = suspendCancellableCoroutine<Unit> { cont ->
    try {
        val mediaPlayer = MediaPlayer.create(context, resId)
        if (mediaPlayer == null) {
            cont.resume(Unit)
            return@suspendCancellableCoroutine
        }
        val attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        mediaPlayer.setAudioAttributes(attr)
        mediaPlayer.setVolume(volume, volume)
        mediaPlayer.setOnCompletionListener {
            it.release()
            if (cont.isActive) cont.resume(Unit)
        }
        mediaPlayer.start()
        cont.invokeOnCancellation {
            try { if (mediaPlayer.isPlaying) mediaPlayer.stop(); mediaPlayer.release() } catch (e: Exception) {}
        }
    } catch (e: Exception) {
        e.printStackTrace()
        if (cont.isActive) cont.resume(Unit)
    }
}