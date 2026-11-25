package com.example.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import com.example.model.ClockFont

object ClockFonts {
    val OrbitronFamily = FontFamily(
        Font(R.font.orbitron, FontWeight.Normal),
        Font(R.font.orbitron, FontWeight.Bold)
    )

    val ShareTechMonoFamily = FontFamily(
        Font(R.font.share_tech_mono, FontWeight.Normal)
    )

    val VT323Family = FontFamily(
        Font(R.font.vt323, FontWeight.Normal)
    )

    val RobotoMonoFamily = FontFamily(
        Font(R.font.roboto_mono, FontWeight.Normal),
        Font(R.font.roboto_mono, FontWeight.Bold)
    )

    fun getFontFamily(clockFont: ClockFont): FontFamily {
        return when (clockFont) {
            ClockFont.ORBITRON -> OrbitronFamily
            ClockFont.SHARE_TECH_MONO -> ShareTechMonoFamily
            ClockFont.VT323 -> VT323Family
            ClockFont.ROBOTO_MONO -> RobotoMonoFamily
            ClockFont.DEFAULT_MONOSPACE -> FontFamily.Monospace
            ClockFont.DEFAULT_SANS -> FontFamily.SansSerif
            ClockFont.DEFAULT_SERIF -> FontFamily.Serif
        }
    }
}
