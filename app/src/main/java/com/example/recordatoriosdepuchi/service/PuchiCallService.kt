package com.example.recordatoriosdepuchi.service

import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import com.example.recordatoriosdepuchi.data.local.PuchiDatabase
import com.example.recordatoriosdepuchi.ui.CallActivity
import com.example.recordatoriosdepuchi.ui.IncomingCallActivity
import com.example.recordatoriosdepuchi.utils.CallContext
import com.example.recordatoriosdepuchi.utils.PreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Servicio de telecomunicaciones que reemplaza la gestión de llamadas nativa de Android.
 * * OBJETIVO SOCIAL:
 * Proporcionar una capa de seguridad para personas mayores, filtrando llamadas
 * de números desconocidos (posible spam/fraude) y mostrando una interfaz simplificada
 * con fotos grandes para los contactos conocidos (familiares).
 */
class PuchiCallService : InCallService() {

    companion object {
        // Referencia estática a la llamada actual para controlarla desde la UI
        var currentCall: Call? = null
    }

    /**
     * Se ejecuta cuando el sistema detecta una nueva llamada (entrante o saliente).
     */
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        if (call.state == Call.STATE_RINGING) {
            handleIncomingCall(call)
        } else {
            handleOutgoingCall(call)
        }
    }

    /**
     * Lógica de seguridad para llamadas entrantes.
     * Consulta la base de datos local para verificar si el número es de confianza.
     */
    private fun handleIncomingCall(call: Call) {
        // Obtenemos el número crudo (ej: tel:+34...)
        val numberRaw = call.details.handle?.schemeSpecificPart ?: ""
        // Normalización: Extraemos solo los dígitos finales para evitar problemas de prefijos
        val number = numberRaw.filter { it.isDigit() }.takeLast(9)

        val db = PuchiDatabase.getDatabase(applicationContext)

        // Ejecutamos en hilo secundario (IO) para no bloquear el hilo principal del servicio
        CoroutineScope(Dispatchers.IO).launch {
            // CONSULTA SEGURA: Buscamos coincidencia en la agenda
            val contact = try {
                if (number.isNotEmpty()) {
                    db.contactDao().findContactByNumber(number)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            if (contact != null) {
                // --- CONTACTO CONOCIDO (LISTA BLANCA) ---
                currentCall = call
                CallContext.currentContact = contact

                // Lanzamos la Activity personalizada con fotos grandes y botones simplificados
                val intent = Intent(this@PuchiCallService, IncomingCallActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
            } else {
                // --- NÚMERO DESCONOCIDO (LISTA NEGRA IMPLÍCITA) ---
                // Rechazamos la llamada automáticamente para proteger al usuario.
                call.reject(false, null)
            }
        }
    }

    /**
     * Gestión de llamadas salientes iniciadas desde la App.
     */
    private fun handleOutgoingCall(call: Call) {
        currentCall = call

        // Configuración automática de audio según preferencias (Altavoz vs Auricular)
        // Esto ayuda a usuarios con problemas de audición o motricidad.
        val useSpeaker = PreferenceHelper.isSpeakerEnabled(this)
        if (useSpeaker) {
            setAudioRoute(CallAudioState.ROUTE_SPEAKER)
        } else {
            setAudioRoute(CallAudioState.ROUTE_EARPIECE)
        }

        try {
            // Lanzamos la UI de llamada en curso
            val intent = Intent(this, CallActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        currentCall = null
    }
}