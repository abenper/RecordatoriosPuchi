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

class PuchiCallService : InCallService() {

    companion object {
        var currentCall: Call? = null
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        if (call.state == Call.STATE_RINGING) {
            handleIncomingCall(call)
        } else {
            handleOutgoingCall(call)
        }
    }

    private fun handleIncomingCall(call: Call) {
        val numberRaw = call.details.handle?.schemeSpecificPart ?: ""
        val number = numberRaw.filter { it.isDigit() }.takeLast(9)
        val db = PuchiDatabase.getDatabase(applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            val contact = try {
                if (number.isNotEmpty()) db.contactDao().findContactByNumber(number) else null
            } catch (e: Exception) { null }

            if (contact != null) {
                currentCall = call
                CallContext.currentContact = contact
                val intent = Intent(this@PuchiCallService, IncomingCallActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
            } else {
                call.reject(false, null)
            }
        }
    }

    private fun handleOutgoingCall(call: Call) {
        currentCall = call

        // Configuración de audio (Altavoz)
        val useSpeaker = PreferenceHelper.isSpeakerEnabled(this)
        if (useSpeaker) {
            setAudioRoute(CallAudioState.ROUTE_SPEAKER)
        } else {
            setAudioRoute(CallAudioState.ROUTE_EARPIECE)
        }

        // --- MEJORA: Buscamos el contacto también al llamar para mostrar foto ---
        val numberRaw = call.details.handle?.schemeSpecificPart ?: ""
        val number = numberRaw.filter { it.isDigit() }.takeLast(9)
        val db = PuchiDatabase.getDatabase(applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            val contact = try {
                if (number.isNotEmpty()) db.contactDao().findContactByNumber(number) else null
            } catch (e: Exception) { null }

            // Guardamos el contacto (o null) en el contexto global
            CallContext.currentContact = contact

            // Ahora que tenemos los datos, lanzamos la pantalla de llamada
            try {
                val intent = Intent(this@PuchiCallService, CallActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        currentCall = null
    }
}