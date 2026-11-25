package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ClockFont
import com.example.model.ClockSettings
import com.example.model.OrientationPreference
import com.example.model.PRESET_COLORS
import com.example.ui.theme.ClockFonts
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: ClockSettings,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit,
    onFontSelected: (ClockFont) -> Unit,
    onToggle24Hour: (Boolean) -> Unit,
    onToggleSeconds: (Boolean) -> Unit,
    onToggleDate: (Boolean) -> Unit,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    onToggleAutoNightMode: (Boolean) -> Unit,
    onUpdateNightHours: (Int, Int) -> Unit,
    onUpdateNormalBrightness: (Float) -> Unit,
    onUpdateNightBrightness: (Float) -> Unit,
    onPersistBrightness: () -> Unit,
    onToggleNightRedShift: (Boolean) -> Unit,
    onToggleBurnInProtection: (Boolean) -> Unit,
    onUpdateOrientation: (OrientationPreference) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF121418),
        scrimColor = Color.Black.copy(alpha = 0.75f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 650.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = settings.textColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Laikrodžio nustatymai",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Uždaryti",
                        tint = Color.LightGray
                    )
                }
            }

            // 1. Text Color Section
            SectionHeader(iconRes = R.drawable.ic_palette, title = "Skaitmenų spalva")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PRESET_COLORS.forEach { option ->
                    val isSelected = option.color.toArgb() == settings.textColor.toArgb()
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(option.color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(option.color) }
                            .testTag("color_preset_${option.name.lowercase().replace(" ", "_")}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Pasirinkta",
                                tint = if (option.color.luminance() > 0.5f) Color.Black else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Custom Color Input (#FC2407 or any custom HEX)
            var hexInput by remember { mutableStateOf("") }
            var isCustomColorError by remember { mutableStateOf(false) }
            val parsedCustomColor = remember(hexInput) { parseHexColor(hexInput) }
            val applyButtonBg = parsedCustomColor ?: settings.textColor
            val applyButtonContent = if (applyButtonBg.luminance() > 0.5f) Color.Black else Color.White

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF171A21),
                border = BorderStroke(1.dp, Color(0xFF262A34)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Pasirinktinė spalva (HEX)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = hexInput,
                            onValueChange = {
                                hexInput = it
                                isCustomColorError = false
                            },
                            placeholder = { Text("#FC2407", color = Color(0xFF6B7280)) },
                            singleLine = true,
                            isError = isCustomColorError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = settings.textColor,
                                unfocusedBorderColor = Color(0xFF374151),
                                errorBorderColor = Color(0xFFFF5252)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_hex_input")
                        )

                        if (parsedCustomColor != null) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(parsedCustomColor)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }

                        Button(
                            onClick = {
                                val color = parseHexColor(hexInput)
                                if (color != null) {
                                    onColorSelected(color)
                                    isCustomColorError = false
                                } else {
                                    isCustomColorError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = applyButtonBg),
                            modifier = Modifier.testTag("apply_custom_color_button")
                        ) {
                            Text(
                                text = "Taikyti",
                                color = applyButtonContent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isCustomColorError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Neteisingas HEX formatas (įveskite pvz. #FC2407)",
                            color = Color(0xFFFF5252),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Greitas pasirinkimas:",
                            color = Color(0xFF9CA3AF),
                            fontSize = 12.sp
                        )
                        TextButton(
                            onClick = {
                                hexInput = "#FC2407"
                                onColorSelected(Color(0xFFFC2407))
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFC2407)),
                            modifier = Modifier.testTag("shortcut_fc2407")
                        ) {
                            Text("#FC2407")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Font Selection Section
            SectionHeader(iconRes = R.drawable.ic_format_paint, title = "Skaitmenų šriftas")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClockFont.entries.forEach { fontOption ->
                    val isSelected = settings.selectedFont == fontOption
                    val sampleFontFamily = ClockFonts.getFontFamily(fontOption)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF1F2937) else Color(0xFF171A21),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) settings.textColor else Color(0xFF272B36)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFontSelected(fontOption) }
                            .testTag("font_option_${fontOption.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = fontOption.displayName,
                                    color = if (isSelected) settings.textColor else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "12:34:56",
                                    color = if (isSelected) settings.textColor else Color(0xFF9CA3AF),
                                    fontSize = 20.sp,
                                    fontFamily = sampleFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = settings.textColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Night Mode Section
            SectionHeader(iconRes = R.drawable.ic_bedtime, title = "Naktinis režimas ir pritemdymas")
            SettingsCard {
                SettingsToggleRow(
                    title = "Automatinis naktinis režimas",
                    subtitle = "Automatiškai pritemdo ekraną nakties metu (${settings.nightStartHour}:00 - ${settings.nightEndHour}:00)",
                    checked = settings.autoNightModeEnabled,
                    onCheckedChange = onToggleAutoNightMode,
                    tag = "auto_night_mode_toggle"
                )

                if (settings.autoNightModeEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nakties valandos: nuo ${settings.nightStartHour}:00 iki ${settings.nightEndHour}:00",
                        color = Color(0xFF9CA3AF),
                        fontSize = 13.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { onUpdateNightHours(22, 7) },
                            colors = ButtonDefaults.textButtonColors(contentColor = settings.textColor)
                        ) {
                            Text("22:00 – 07:00")
                        }
                        TextButton(
                            onClick = { onUpdateNightHours(23, 6) },
                            colors = ButtonDefaults.textButtonColors(contentColor = settings.textColor)
                        ) {
                            Text("23:00 – 06:00")
                        }
                        TextButton(
                            onClick = { onUpdateNightHours(21, 8) },
                            colors = ButtonDefaults.textButtonColors(contentColor = settings.textColor)
                        ) {
                            Text("21:00 – 08:00")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SettingsToggleRow(
                    title = "Raudona naktinė šviesa",
                    subtitle = "Naktį pakeičia spalvą į švelnią raudoną/gintarinę, saugančią akis tamsoje",
                    checked = settings.useNightRedShift,
                    onCheckedChange = onToggleNightRedShift,
                    tag = "night_red_shift_toggle"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Naktinio režimo ryškumas",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${(settings.nightBrightness * 100).roundToInt()}%",
                            color = settings.textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = settings.nightBrightness,
                        onValueChange = onUpdateNightBrightness,
                        onValueChangeFinished = onPersistBrightness,
                        valueRange = 0.01f..0.30f,
                        colors = SliderDefaults.colors(
                            thumbColor = settings.textColor,
                            activeTrackColor = settings.textColor,
                            inactiveTrackColor = Color(0xFF2D3748)
                        ),
                        modifier = Modifier.testTag("night_brightness_slider")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Dieninis ekrano ryškumas",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${(settings.normalBrightness * 100).roundToInt()}%",
                            color = settings.textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = settings.normalBrightness,
                        onValueChange = onUpdateNormalBrightness,
                        onValueChangeFinished = onPersistBrightness,
                        valueRange = 0.20f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = settings.textColor,
                            activeTrackColor = settings.textColor,
                            inactiveTrackColor = Color(0xFF2D3748)
                        ),
                        modifier = Modifier.testTag("normal_brightness_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Screen & Orientation Section
            SectionHeader(iconRes = R.drawable.ic_screen_rotation, title = "Ekrano padėtis ir apsauga")
            SettingsCard {
                SettingsToggleRow(
                    title = "Niekada neišjungti ekrano",
                    subtitle = "Ekranas lieka visada įjungtas (Keep screen on)",
                    checked = settings.keepScreenOn,
                    onCheckedChange = onToggleKeepScreenOn,
                    tag = "keep_screen_on_toggle"
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsToggleRow(
                    title = "OLED apsauga nuo išdegimo",
                    subtitle = "Kas minutę nežymiai paslenka skaitmenis po kelis pikselius",
                    checked = settings.burnInProtection,
                    onCheckedChange = onToggleBurnInProtection,
                    tag = "burn_in_protection_toggle"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Ekrano orientacija",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                OrientationPreference.entries.forEach { pref ->
                    val isSelected = settings.orientationPref == pref
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onUpdateOrientation(pref) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) settings.textColor else Color.Gray,
                                    shape = CircleShape
                                )
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(settings.textColor, CircleShape)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = pref.title,
                            color = if (isSelected) Color.White else Color(0xFFD1D5DB),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Time Format Section
            SectionHeader(iconRes = R.drawable.ic_tune, title = "Laiko rodymas")
            SettingsCard {
                SettingsToggleRow(
                    title = "24 valandų formatas",
                    subtitle = if (settings.is24Hour) "23:59 formatas" else "11:59 PM formatas",
                    checked = settings.is24Hour,
                    onCheckedChange = onToggle24Hour,
                    tag = "toggle_24_hour"
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsToggleRow(
                    title = "Rodyti sekundes",
                    subtitle = "Rodyti smulkesnius sekundžių skaitmenis",
                    checked = settings.showSeconds,
                    onCheckedChange = onToggleSeconds,
                    tag = "toggle_seconds"
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsToggleRow(
                    title = "Rodyti datą",
                    subtitle = "Rodyti savaitės dieną ir mėnesio datą apačioje",
                    checked = settings.showDate,
                    onCheckedChange = onToggleDate,
                    tag = "toggle_date"
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    @DrawableRes iconRes: Int,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            color = Color(0xFFD1D5DB),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF171A21),
        border = BorderStroke(1.dp, Color(0xFF262A34)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = Color(0xFF9CA3AF),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF10B981),
                uncheckedThumbColor = Color(0xFF9CA3AF),
                uncheckedTrackColor = Color(0xFF374151)
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}

fun parseHexColor(input: String): Color? {
    val clean = input.trim().removePrefix("#").removePrefix("0x")
    return when (clean.length) {
        6 -> try {
            val r = clean.substring(0, 2).toInt(16)
            val g = clean.substring(2, 4).toInt(16)
            val b = clean.substring(4, 6).toInt(16)
            Color(red = r, green = g, blue = b)
        } catch (_: Exception) {
            null
        }
        8 -> try {
            val a = clean.substring(0, 2).toInt(16)
            val r = clean.substring(2, 4).toInt(16)
            val g = clean.substring(4, 6).toInt(16)
            val b = clean.substring(6, 8).toInt(16)
            Color(red = r, green = g, blue = b, alpha = a)
        } catch (_: Exception) {
            null
        }
        else -> null
    }
}
