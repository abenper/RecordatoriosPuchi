package com.example.recordatoriosdepuchi.utils

import android.content.Context

object PreferenceHelper {
    private const val PREFS_NAME = "puchi_prefs"
    private const val KEY_USE_SPEAKER = "use_speaker"

    // --- CLAVES DE CONFIGURACIÓN ---
    private const val KEY_CONTACT_ORDER = "contact_order"
    private const val KEY_SCREENSAVER_TIMEOUT = "screensaver_timeout"
    private const val KEY_SHOW_PUPPET = "show_puppet"
    private const val KEY_PUPPET_INTERVAL = "puppet_interval"

    // NUEVAS CLAVES DE VOLUMEN
    private const val KEY_VOL_MASTER_RING = "vol_master_ring"
    private const val KEY_VOL_MASTER_VOICE = "vol_master_voice"
    private const val KEY_VOL_MASTER_EFFECTS = "vol_master_effects"

    // NUEVA CLAVE: MÚSICA DE FONDO
    private const val KEY_VOL_BACKGROUND_MUSIC = "vol_background_music"

    private fun getPrefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // AUDIO
    fun isSpeakerEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_USE_SPEAKER, true)
    fun setSpeakerEnabled(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_USE_SPEAKER, enabled).apply()

    // --- ORDEN DE CONTACTOS (CORREGIDO PARA USAR INT) ---
    fun getContactOrder(context: Context): List<Int> {
        val str = getPrefs(context).getString(KEY_CONTACT_ORDER, "") ?: ""
        if (str.isEmpty()) return emptyList()
        // Convertimos la cadena "1,2,3" en una lista de Enteros [1, 2, 3]
        return str.split(",").mapNotNull { it.toIntOrNull() }
    }

    fun setContactOrder(context: Context, ids: List<Int>) {
        // Guardamos la lista de Enteros como cadena "1,2,3"
        val str = ids.joinToString(",")
        getPrefs(context).edit().putString(KEY_CONTACT_ORDER, str).apply()
    }

    // SALVAPANTALLAS
    fun getScreensaverTimeout(context: Context): Int = getPrefs(context).getInt(KEY_SCREENSAVER_TIMEOUT, 30)
    fun setScreensaverTimeout(context: Context, seconds: Int) = getPrefs(context).edit().putInt(KEY_SCREENSAVER_TIMEOUT, seconds).apply()

    // ASISTENTE (MUÑECO)
    fun isPuppetEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_SHOW_PUPPET, true)
    fun setPuppetEnabled(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_SHOW_PUPPET, enabled).apply()

    fun getPuppetInterval(context: Context): Int = getPrefs(context).getInt(KEY_PUPPET_INTERVAL, 15)
    fun setPuppetInterval(context: Context, minutes: Int) = getPrefs(context).edit().putInt(KEY_PUPPET_INTERVAL, minutes).apply()

    // --- VOLÚMENES DE AUDIO ---
    fun getRingVolume(context: Context): Float = getPrefs(context).getFloat(KEY_VOL_MASTER_RING, 1.0f)
    fun setRingVolume(context: Context, volume: Float) = getPrefs(context).edit().putFloat(KEY_VOL_MASTER_RING, volume.coerceIn(0f, 1f)).apply()

    fun getVoiceVolume(context: Context): Float = getPrefs(context).getFloat(KEY_VOL_MASTER_VOICE, 1.0f)
    fun setVoiceVolume(context: Context, volume: Float) = getPrefs(context).edit().putFloat(KEY_VOL_MASTER_VOICE, volume.coerceIn(0f, 1f)).apply()

    fun getEffectsVolume(context: Context): Float = getPrefs(context).getFloat(KEY_VOL_MASTER_EFFECTS, 0.5f)
    fun setEffectsVolume(context: Context, volume: Float) = getPrefs(context).edit().putFloat(KEY_VOL_MASTER_EFFECTS, volume.coerceIn(0f, 1f)).apply()

    // NUEVO: VOLUMEN MÚSICA DE FONDO (Por defecto muy bajito: 0.05f)
    fun getBackgroundMusicVolume(context: Context): Float = getPrefs(context).getFloat(KEY_VOL_BACKGROUND_MUSIC, 0.05f)
    fun setBackgroundMusicVolume(context: Context, volume: Float) = getPrefs(context).edit().putFloat(KEY_VOL_BACKGROUND_MUSIC, volume.coerceIn(0f, 1f)).apply()
}