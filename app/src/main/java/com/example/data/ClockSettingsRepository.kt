package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.model.ClockFont
import com.example.model.ClockSettings
import com.example.model.OrientationPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ClockSettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("digital_clock_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<ClockSettings> = _settings.asStateFlow()

    private fun loadSettings(): ClockSettings {
        val colorInt = prefs.getInt(KEY_TEXT_COLOR, 0xFF00E5FF.toInt())
        val fontName = prefs.getString(KEY_FONT, ClockFont.ORBITRON.name) ?: ClockFont.ORBITRON.name
        val selectedFont = try {
            ClockFont.valueOf(fontName)
        } catch (e: Exception) {
            ClockFont.ORBITRON
        }

        val orientationName = prefs.getString(KEY_ORIENTATION, OrientationPreference.LANDSCAPE_SENSOR.name)
            ?: OrientationPreference.LANDSCAPE_SENSOR.name
        val orientationPref = try {
            OrientationPreference.valueOf(orientationName)
        } catch (e: Exception) {
            OrientationPreference.LANDSCAPE_SENSOR
        }

        return ClockSettings(
            textColor = Color(colorInt),
            selectedFont = selectedFont,
            is24Hour = prefs.getBoolean(KEY_IS_24_HOUR, true),
            showSeconds = prefs.getBoolean(KEY_SHOW_SECONDS, false),
            showDate = prefs.getBoolean(KEY_SHOW_DATE, true),
            keepScreenOn = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true),
            autoNightModeEnabled = prefs.getBoolean(KEY_AUTO_NIGHT_MODE, true),
            nightStartHour = prefs.getInt(KEY_NIGHT_START, 22),
            nightEndHour = prefs.getInt(KEY_NIGHT_END, 7),
            isManualNightMode = false,
            nightBrightness = prefs.getFloat(KEY_NIGHT_BRIGHTNESS, 0.05f),
            normalBrightness = prefs.getFloat(KEY_NORMAL_BRIGHTNESS, 0.85f),
            useNightRedShift = prefs.getBoolean(KEY_NIGHT_RED_SHIFT, true),
            burnInProtection = prefs.getBoolean(KEY_BURN_IN_PROTECTION, true),
            orientationPref = orientationPref
        )
    }

    fun updateSettings(newSettings: ClockSettings, persist: Boolean = true) {
        _settings.value = newSettings
        if (persist) {
            saveToPrefs(newSettings)
        }
    }

    fun persistCurrent() {
        saveToPrefs(_settings.value)
    }

    private fun saveToPrefs(newSettings: ClockSettings) {
        prefs.edit().apply {
            putInt(KEY_TEXT_COLOR, newSettings.textColor.toArgb())
            putString(KEY_FONT, newSettings.selectedFont.name)
            putBoolean(KEY_IS_24_HOUR, newSettings.is24Hour)
            putBoolean(KEY_SHOW_SECONDS, newSettings.showSeconds)
            putBoolean(KEY_SHOW_DATE, newSettings.showDate)
            putBoolean(KEY_KEEP_SCREEN_ON, newSettings.keepScreenOn)
            putBoolean(KEY_AUTO_NIGHT_MODE, newSettings.autoNightModeEnabled)
            putInt(KEY_NIGHT_START, newSettings.nightStartHour)
            putInt(KEY_NIGHT_END, newSettings.nightEndHour)
            putFloat(KEY_NIGHT_BRIGHTNESS, newSettings.nightBrightness)
            putFloat(KEY_NORMAL_BRIGHTNESS, newSettings.normalBrightness)
            putBoolean(KEY_NIGHT_RED_SHIFT, newSettings.useNightRedShift)
            putBoolean(KEY_BURN_IN_PROTECTION, newSettings.burnInProtection)
            putString(KEY_ORIENTATION, newSettings.orientationPref.name)
            apply()
        }
    }

    companion object {
        private const val KEY_TEXT_COLOR = "text_color"
        private const val KEY_FONT = "clock_font"
        private const val KEY_IS_24_HOUR = "is_24_hour"
        private const val KEY_SHOW_SECONDS = "show_seconds"
        private const val KEY_SHOW_DATE = "show_date"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_AUTO_NIGHT_MODE = "auto_night_mode"
        private const val KEY_NIGHT_START = "night_start"
        private const val KEY_NIGHT_END = "night_end"
        private const val KEY_NIGHT_BRIGHTNESS = "night_brightness"
        private const val KEY_NORMAL_BRIGHTNESS = "normal_brightness"
        private const val KEY_NIGHT_RED_SHIFT = "night_red_shift"
        private const val KEY_BURN_IN_PROTECTION = "burn_in_protection"
        private const val KEY_ORIENTATION = "orientation_pref"
    }
}
