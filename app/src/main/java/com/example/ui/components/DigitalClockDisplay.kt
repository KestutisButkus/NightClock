package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClockSettings
import com.example.ui.BurnInOffset
import com.example.ui.TimeDisplayData
import com.example.ui.theme.ClockFonts

@Composable
fun DigitalClockDisplay(
    timeData: TimeDisplayData,
    settings: ClockSettings,
    burnInOffset: BurnInOffset,
    targetColor: Color,
    isNightMode: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 800),
        label = "clockColor"
    )

    val fontFamily = remember(settings.selectedFont) {
        ClockFonts.getFontFamily(settings.selectedFont)
    }

    // Padding: 5dp when OLED protection is disabled, 10dp when OLED protection is enabled; margins 0px
    val clockPadding = if (settings.burnInProtection) 10.dp else 5.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("digital_clock_display"),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()

        val maxAvailableWidthPx = with(density) { (maxWidth - (clockPadding * 2)).toPx() }
        val dateHeightReservePx = if (settings.showDate) with(density) { 32.dp.toPx() } else 0f
        val maxAvailableHeightPx = with(density) { (maxHeight - (clockPadding * 2)).toPx() } - dateHeightReservePx

        // Use a fixed widest-digit reference template ("00:00") so proportional fonts like Orbitron
        // never jitter in size when digits change (e.g., 1 -> 2), and recompute only when layout/settings change.
        val optimalFontSize = remember(
            maxWidth,
            maxHeight,
            settings.showSeconds,
            settings.is24Hour,
            settings.selectedFont,
            settings.burnInProtection,
            settings.showDate
        ) {
            val mainRef = textMeasurer.measure(
                text = "00:00",
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 100.sp,
                    letterSpacing = (-1).sp
                ),
                constraints = Constraints()
            )

            var totalWidth = mainRef.size.width.toFloat()
            val measuredHeight = mainRef.size.height.toFloat().coerceAtLeast(10f)

            if (settings.showSeconds) {
                val secRef = textMeasurer.measure(
                    text = ":00",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 38.sp
                    ),
                    constraints = Constraints()
                )
                totalWidth += secRef.size.width.toFloat()
            }

            if (!settings.is24Hour) {
                val amPmRef = textMeasurer.measure(
                    text = " PM",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    ),
                    constraints = Constraints()
                )
                totalWidth += amPmRef.size.width.toFloat()
            }

            val measuredWidth = totalWidth.coerceAtLeast(10f)
            val targetWidthPx = maxAvailableWidthPx * 0.98f
            val scaleX = targetWidthPx / measuredWidth
            val scaleY = (maxAvailableHeightPx * 0.95f) / measuredHeight

            val chosenScale = minOf(scaleX, scaleY).coerceIn(0.2f, 10.0f)
            (100f * chosenScale).sp
        }

        val shiftModifier = if (settings.burnInProtection) {
            Modifier.offset(
                x = burnInOffset.offsetX.dp,
                y = burnInOffset.offsetY.dp
            )
        } else {
            Modifier
        }

        Column(
            modifier = shiftModifier.padding(clockPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timeData.hoursMinutes,
                    color = animatedColor,
                    fontSize = optimalFontSize,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-1).sp,
                    lineHeight = optimalFontSize,
                    maxLines = 1,
                    softWrap = false
                )

                if (settings.showSeconds) {
                    val secondsFontSize = (optimalFontSize.value * 0.38f).sp
                    Text(
                        text = ":${timeData.seconds}",
                        color = animatedColor.copy(alpha = if (isNightMode) 0.6f else 0.85f),
                        fontSize = secondsFontSize,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(bottom = (optimalFontSize.value * 0.10f).dp)
                    )
                }

                if (timeData.amPm != null) {
                    val amPmFontSize = (optimalFontSize.value * 0.20f).sp
                    Text(
                        text = " ${timeData.amPm}",
                        color = animatedColor.copy(alpha = if (isNightMode) 0.5f else 0.75f),
                        fontSize = amPmFontSize,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(bottom = (optimalFontSize.value * 0.12f).dp)
                    )
                }
            }

            if (settings.showDate && timeData.dateString.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val dateAlpha = if (isNightMode) 0.35f else 0.65f
                Text(
                    text = timeData.dateString,
                    color = animatedColor.copy(alpha = dateAlpha),
                    fontSize = 15.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
