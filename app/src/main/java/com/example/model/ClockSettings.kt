package com.example.model

import androidx.compose.ui.graphics.Color

enum class ClockFont(val displayName: String, val fontResName: String?) {
    ORBITRON("Orbitron (Sci-Fi)", "orbitron"),
    SHARE_TECH_MONO("Share Tech (Terminal)", "share_tech_mono"),
    VT323("VT323 (Retro LED)", "vt323"),
    ROBOTO_MONO("Roboto Mono (Švarus)", "roboto_mono"),
    DEFAULT_MONOSPACE("Monospace (Sistemos)", null),
    DEFAULT_SANS("Sans-Serif (Modernus)", null),
    DEFAULT_SERIF("Serif (Klasikinis)", null)
}

data class ColorOption(
    val name: String,
    val color: Color
)

val PRESET_COLORS = listOf(
    ColorOption("Ugninga Raudona (#FC2407)", Color(0xFFFC2407)),
    ColorOption("Neoninė Žalia", Color(0xFF00FF66)),
    ColorOption("Kibernetinė Žydra", Color(0xFF00E5FF)),
    ColorOption("Gintarinė / Oranžinė", Color(0xFFFF9100)),
    ColorOption("Naktinė Raudona", Color(0xFFFF1744)),
    ColorOption("Minkšta Balta", Color(0xFFE2E8F0)),
    ColorOption("Gelsva / Citrina", Color(0xFFFFEE58)),
    ColorOption("Ledo Mėlyna", Color(0xFF40C4FF)),
    ColorOption("Neoninė Violetinė", Color(0xFFD500F9)),
    ColorOption("Rožinė Magentinė", Color(0xFFFF4081)),
    ColorOption("Mėtos Žalia", Color(0xFF1DE9B6))
)

enum class OrientationPreference(val title: String) {
    LANDSCAPE_SENSOR("Automatinis horizontalus (rekomenduojama)"),
    LANDSCAPE_FIXED("Užrakintas horizontaliai"),
    SENSOR_ALL("Laisvas pasukimas"),
    PORTRAIT("Vertikalus")
}

data class ClockSettings(
    val textColor: Color = Color(0xFF00E5FF),
    val selectedFont: ClockFont = ClockFont.ORBITRON,
    val is24Hour: Boolean = true,
    val showSeconds: Boolean = false,
    val showDate: Boolean = true,
    val keepScreenOn: Boolean = true,
    // Night mode settings
    val autoNightModeEnabled: Boolean = true,
    val nightStartHour: Int = 22, // 22:00
    val nightEndHour: Int = 7,   // 07:00
    val isManualNightMode: Boolean = false,
    val nightBrightness: Float = 0.05f, // Dim physical screen
    val normalBrightness: Float = 0.85f,
    val useNightRedShift: Boolean = true, // Shift color to dim red/amber at night
    val burnInProtection: Boolean = true, // Pixel shift every minute
    val orientationPref: OrientationPreference = OrientationPreference.LANDSCAPE_SENSOR
)
