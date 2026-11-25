package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.receiver.BatteryInfo

/**
 * Baterijos indikatoriaus parametrai:
 * - Visiškai pašalintas prislopinimas (100% pilnas ryškumas)
 * - Baterijos korpuso dydis: 27.dp x 13.5.dp
 * - Įkrovimo žaibo ikona: 21.dp
 * - Procentų šrifto dydis: 18.sp
 * - Tarpas tarp elementų: 6.dp
 */
@Composable
fun BatteryIndicator(
    batteryInfo: BatteryInfo,
    modifier: Modifier = Modifier
) {
    val indicatorColor = when {
        batteryInfo.percentage <= 15 -> Color(0xFFFF5252)
        batteryInfo.isCharging -> Color(0xFF00E676)
        else -> Color.White
    }

    Row(
        modifier = modifier
            .testTag("battery_indicator")
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (batteryInfo.isCharging) {
            Icon(
                painter = painterResource(R.drawable.ic_bolt),
                contentDescription = "Kraunasi",
                tint = indicatorColor,
                modifier = Modifier.size(21.dp)
            )
        }

        Canvas(modifier = Modifier.size(width = 27.dp, height = 13.5.dp)) {
            val strokeW = 1.5.dp.toPx()
            val corner = 3.dp.toPx()
            val terminalW = 3.dp.toPx()
            val terminalH = 6.dp.toPx()

            val bodyW = size.width - terminalW - 1.5.dp.toPx()
            val bodyH = size.height

            drawRoundRect(
                color = indicatorColor,
                topLeft = Offset(0f, 0f),
                size = Size(bodyW, bodyH),
                cornerRadius = CornerRadius(corner, corner),
                style = Stroke(width = strokeW)
            )

            drawRoundRect(
                color = indicatorColor,
                topLeft = Offset(bodyW, (bodyH - terminalH) / 2f),
                size = Size(terminalW, terminalH),
                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
            )

            val fillPadding = strokeW * 1.5f
            val maxFillW = bodyW - (fillPadding * 2)
            val fillH = bodyH - (fillPadding * 2)
            val fillW = (maxFillW * (batteryInfo.percentage.coerceIn(0, 100) / 100f)).coerceAtLeast(0f)

            if (fillW > 0) {
                drawRoundRect(
                    color = indicatorColor,
                    topLeft = Offset(fillPadding, fillPadding),
                    size = Size(fillW, fillH),
                    cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                )
            }
        }

        Text(
            text = "${batteryInfo.percentage}%",
            color = indicatorColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = (-0.2).sp
        )
    }
}
