package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Settings data model ported from dsh-pet-android preferences architecture.
 * Adapted for standalone native Android desktop pet without DSH host dependencies.
 */
data class PetSettings(
    val petScale: Float = 1.0f,
    val hapticEnabled: Boolean = true,
    val squashEnabled: Boolean = true,
    val autoIdleDurationSec: Int = 10,
    val keepScreenOn: Boolean = true,
    val comboWindowMs: Long = 1800L
)

class PetPreferences(context: Context) {
    private val sp: SharedPreferences =
        context.applicationContext.getSharedPreferences("bigfish_pet_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<PetSettings> = _settings.asStateFlow()

    private fun loadSettings(): PetSettings {
        return PetSettings(
            petScale = sp.getFloat(KEY_SCALE, 1.0f).coerceIn(0.7f, 1.5f),
            hapticEnabled = sp.getBoolean(KEY_HAPTIC, true),
            squashEnabled = sp.getBoolean(KEY_SQUASH, true),
            autoIdleDurationSec = sp.getInt(KEY_AUTO_IDLE, 10),
            keepScreenOn = sp.getBoolean(KEY_KEEP_SCREEN_ON, true),
            comboWindowMs = sp.getLong(KEY_COMBO_WINDOW, 1800L)
        )
    }

    fun updatePetScale(scale: Float) {
        val clamped = (Math.round(scale.coerceIn(0.7f, 1.5f) * 100f) / 100f)
        sp.edit().putFloat(KEY_SCALE, clamped).apply()
        _settings.update { it.copy(petScale = clamped) }
    }

    fun updateHapticEnabled(enabled: Boolean) {
        sp.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _settings.update { it.copy(hapticEnabled = enabled) }
    }

    fun updateSquashEnabled(enabled: Boolean) {
        sp.edit().putBoolean(KEY_SQUASH, enabled).apply()
        _settings.update { it.copy(squashEnabled = enabled) }
    }

    fun updateAutoIdleDuration(seconds: Int) {
        val valid = if (seconds in listOf(5, 10, 15)) seconds else 10
        sp.edit().putInt(KEY_AUTO_IDLE, valid).apply()
        _settings.update { it.copy(autoIdleDurationSec = valid) }
    }

    fun updateKeepScreenOn(enabled: Boolean) {
        sp.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
        _settings.update { it.copy(keepScreenOn = enabled) }
    }

    fun updateComboWindow(ms: Long) {
        val valid = if (ms in listOf(1200L, 1800L, 2500L)) ms else 1800L
        sp.edit().putLong(KEY_COMBO_WINDOW, valid).apply()
        _settings.update { it.copy(comboWindowMs = valid) }
    }

    fun resetToDefaults() {
        sp.edit().clear().apply()
        _settings.value = PetSettings()
    }

    companion object {
        private const val KEY_SCALE = "pet_scale"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_SQUASH = "squash_enabled"
        private const val KEY_AUTO_IDLE = "auto_idle_sec"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_COMBO_WINDOW = "combo_window_ms"

        @Volatile
        private var INSTANCE: PetPreferences? = null

        fun getInstance(context: Context): PetPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PetPreferences(context).also { INSTANCE = it }
            }
        }
    }
}
