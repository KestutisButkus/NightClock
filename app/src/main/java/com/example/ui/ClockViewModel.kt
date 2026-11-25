package com.example.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ClockSettingsRepository
import com.example.model.ClockFont
import com.example.model.ClockSettings
import com.example.model.OrientationPreference
import com.example.receiver.BatteryInfo
import com.example.receiver.getBatteryFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TimeDisplayData(
    val hoursMinutes: String,
    val seconds: String,
    val amPm: String?,
    val dateString: String,
    val currentHour: Int
)

data class BurnInOffset(
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)

class ClockViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ClockSettingsRepository(application)
    val settings: StateFlow<ClockSettings> = repository.settings

    val batteryInfo: StateFlow<BatteryInfo> = getBatteryFlow(application)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            BatteryInfo()
        )

    private val lithuanianLocale = Locale.forLanguageTag("lt-LT")
    private val format24Hour = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val format12Hour = SimpleDateFormat("hh:mm", Locale.getDefault())
    private val secondsFormat = SimpleDateFormat("ss", Locale.getDefault())
    private val amPmFormat = SimpleDateFormat("a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEEE, MMMM d 'd.'", lithuanianLocale)
    private val calendar = Calendar.getInstance()
    private val reusableDate = Date()

    private val _timeData = MutableStateFlow(getCurrentTimeDisplayData(settings.value))
    val timeData: StateFlow<TimeDisplayData> = _timeData.asStateFlow()

    private val _burnInOffset = MutableStateFlow(BurnInOffset())
    val burnInOffset: StateFlow<BurnInOffset> = _burnInOffset.asStateFlow()

    private val _areControlsVisible = MutableStateFlow(true)
    val areControlsVisible: StateFlow<Boolean> = _areControlsVisible.asStateFlow()

    private val _isSettingsSheetOpen = MutableStateFlow(false)
    val isSettingsSheetOpen: StateFlow<Boolean> = _isSettingsSheetOpen.asStateFlow()

    private var hideControlsJob: Job? = null

    init {
        // Wall-clock aligned ticker; suppresses redundant emissions when seconds are hidden
        viewModelScope.launch {
            while (isActive) {
                _timeData.value = getCurrentTimeDisplayData(settings.value)
                val nextSecondDelayMs = (1000L - (System.currentTimeMillis() % 1000L)).coerceIn(50L, 1000L)
                delay(nextSecondDelayMs)
            }
        }

        // OLED burn-in offset shift every 60 seconds (max 3.5dp, safely inside 10dp padding)
        viewModelScope.launch {
            var step = 0
            val shiftCoordinates = listOf(
                BurnInOffset(0f, 0f),
                BurnInOffset(3.5f, -2.5f),
                BurnInOffset(-3.0f, 3.0f),
                BurnInOffset(2.5f, 3.5f),
                BurnInOffset(-3.5f, -2.0f),
                BurnInOffset(2.0f, -3.0f),
                BurnInOffset(-2.5f, 2.5f),
                BurnInOffset(3.0f, 1.5f)
            )
            while (isActive) {
                delay(60_000L)
                if (settings.value.burnInProtection) {
                    step = (step + 1) % shiftCoordinates.size
                    _burnInOffset.value = shiftCoordinates[step]
                } else {
                    _burnInOffset.value = BurnInOffset(0f, 0f)
                }
            }
        }

        resetControlsTimeout()
    }

    private fun getCurrentTimeDisplayData(currentSettings: ClockSettings): TimeDisplayData {
        val nowMillis = System.currentTimeMillis()
        reusableDate.time = nowMillis
        calendar.timeInMillis = nowMillis
        val hour24 = calendar.get(Calendar.HOUR_OF_DAY)

        val hoursMinutes = if (currentSettings.is24Hour) {
            format24Hour.format(reusableDate)
        } else {
            format12Hour.format(reusableDate)
        }

        // Only populate seconds when shown so StateFlow avoids 59 redundant recompositions/min
        val seconds = if (currentSettings.showSeconds) {
            secondsFormat.format(reusableDate)
        } else {
            ""
        }

        val amPm = if (!currentSettings.is24Hour) {
            amPmFormat.format(reusableDate)
        } else {
            null
        }

        val formattedDate = if (currentSettings.showDate) {
            val rawDate = dateFormat.format(reusableDate)
            rawDate.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(lithuanianLocale) else it.toString()
            }
        } else {
            ""
        }

        return TimeDisplayData(
            hoursMinutes = hoursMinutes,
            seconds = seconds,
            amPm = amPm,
            dateString = formattedDate,
            currentHour = hour24
        )
    }

    fun isNightModeActive(current: ClockSettings = settings.value, hour: Int = _timeData.value.currentHour): Boolean {
        if (current.isManualNightMode) return true
        if (!current.autoNightModeEnabled) return false

        return if (current.nightStartHour > current.nightEndHour) {
            hour >= current.nightStartHour || hour < current.nightEndHour
        } else {
            hour in current.nightStartHour until current.nightEndHour
        }
    }

    fun getEffectiveTextColor(current: ClockSettings = settings.value, isNight: Boolean = isNightModeActive()): Color {
        return if (isNight) {
            if (current.useNightRedShift) {
                Color(0x99FF3333)
            } else {
                current.textColor.copy(alpha = 0.35f)
            }
        } else {
            current.textColor
        }
    }

    fun getEffectiveScreenBrightness(current: ClockSettings = settings.value, isNight: Boolean = isNightModeActive()): Float {
        return if (isNight) current.nightBrightness else current.normalBrightness
    }

    fun onScreenTapped() {
        if (_isSettingsSheetOpen.value) return
        val nextVisible = !_areControlsVisible.value
        _areControlsVisible.value = nextVisible
        if (nextVisible) {
            resetControlsTimeout()
        } else {
            hideControlsJob?.cancel()
        }
    }

    fun resetControlsTimeout() {
        hideControlsJob?.cancel()
        hideControlsJob = viewModelScope.launch {
            delay(4000)
            if (!_isSettingsSheetOpen.value) {
                _areControlsVisible.value = false
            }
        }
    }

    fun toggleManualNightMode() {
        val current = settings.value
        repository.updateSettings(current.copy(isManualNightMode = !current.isManualNightMode), persist = false)
        resetControlsTimeout()
    }

    fun openSettings() {
        hideControlsJob?.cancel()
        _areControlsVisible.value = true
        _isSettingsSheetOpen.value = true
    }

    fun closeSettings() {
        _isSettingsSheetOpen.value = false
        resetControlsTimeout()
    }

    fun updateTextColor(color: Color) {
        repository.updateSettings(settings.value.copy(textColor = color))
    }

    fun updateFont(font: ClockFont) {
        repository.updateSettings(settings.value.copy(selectedFont = font))
    }

    fun toggle24Hour(is24: Boolean) {
        val updated = settings.value.copy(is24Hour = is24)
        repository.updateSettings(updated)
        _timeData.value = getCurrentTimeDisplayData(updated)
    }

    fun toggleSeconds(show: Boolean) {
        val updated = settings.value.copy(showSeconds = show)
        repository.updateSettings(updated)
        _timeData.value = getCurrentTimeDisplayData(updated)
    }

    fun toggleDate(show: Boolean) {
        val updated = settings.value.copy(showDate = show)
        repository.updateSettings(updated)
        _timeData.value = getCurrentTimeDisplayData(updated)
    }

    fun toggleKeepScreenOn(keep: Boolean) {
        repository.updateSettings(settings.value.copy(keepScreenOn = keep))
    }

    fun toggleAutoNightMode(enabled: Boolean) {
        repository.updateSettings(settings.value.copy(autoNightModeEnabled = enabled))
    }

    fun updateNightHours(startHour: Int, endHour: Int) {
        repository.updateSettings(settings.value.copy(nightStartHour = startHour, nightEndHour = endHour))
    }

    fun updateNormalBrightness(brightness: Float) {
        repository.updateSettings(settings.value.copy(normalBrightness = brightness), persist = false)
    }

    fun updateNightBrightness(brightness: Float) {
        repository.updateSettings(settings.value.copy(nightBrightness = brightness), persist = false)
    }

    fun persistBrightnessChanges() {
        repository.persistCurrent()
    }

    fun toggleNightRedShift(enabled: Boolean) {
        repository.updateSettings(settings.value.copy(useNightRedShift = enabled))
    }

    fun toggleBurnInProtection(enabled: Boolean) {
        repository.updateSettings(settings.value.copy(burnInProtection = enabled))
        if (!enabled) {
            _burnInOffset.value = BurnInOffset(0f, 0f)
        }
    }

    fun updateOrientation(pref: OrientationPreference) {
        repository.updateSettings(settings.value.copy(orientationPref = pref))
    }
}
