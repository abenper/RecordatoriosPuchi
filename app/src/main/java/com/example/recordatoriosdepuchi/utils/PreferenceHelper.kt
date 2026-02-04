package com.example.recordatoriosdepuchi.utils

import android.content.Context

object PreferenceHelper {
    private const val PREFS_NAME = "puchi_prefs"
    private const val KEY_USE_SPEAKER = "use_speaker"

    // --- NUEVAS CONFIGURACIONES ---
    private const val KEY_CONTACT_ORDER = "contact_order" // Lista de IDs guardada: "1,5,2"
    private const val KEY_SCREENSAVER_TIMEOUT = "screensaver_timeout" // Segundos para activar
    private const val KEY_SHOW_PUPPET = "show_puppet" // Mostrar muñeco SÍ/NO
    private const val KEY_PUPPET_INTERVAL = "puppet_interval" // Minutos entre apariciones

    private fun getPrefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // AUDIO
    fun isSpeakerEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_USE_SPEAKER, true)
    fun setSpeakerEnabled(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_USE_SPEAKER, enabled).apply()

    // ORDEN DE CONTACTOS (Guardamos los IDs separados por comas)
    fun getContactOrder(context: Context): List<String> {
        val str = getPrefs(context).getString(KEY_CONTACT_ORDER, "") ?: ""
        return if (str.isEmpty()) emptyList() else str.split(",")
    }

    fun setContactOrder(context: Context, ids: List<Int>) {
        val str = ids.joinToString(",")
        getPrefs(context).edit().putString(KEY_CONTACT_ORDER, str).apply()
    }

    // SALVAPANTALLAS (Tiempo de espera, por defecto 120s)
    fun getScreensaverTimeout(context: Context): Int = getPrefs(context).getInt(KEY_SCREENSAVER_TIMEOUT, 120)
    fun setScreensaverTimeout(context: Context, seconds: Int) = getPrefs(context).edit().putInt(KEY_SCREENSAVER_TIMEOUT, seconds).apply()

    // MUÑECO (Configuración)
    fun isPuppetEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_SHOW_PUPPET, true)
    fun setPuppetEnabled(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_SHOW_PUPPET, enabled).apply()

    fun getPuppetInterval(context: Context): Int = getPrefs(context).getInt(KEY_PUPPET_INTERVAL, 15) // Por defecto 15 min
    fun setPuppetInterval(context: Context, minutes: Int) = getPrefs(context).edit().putInt(KEY_PUPPET_INTERVAL, minutes).apply()
}