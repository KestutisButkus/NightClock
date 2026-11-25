package com.example.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.OrientationPreference
import com.example.ui.components.BatteryIndicator
import com.example.ui.components.DigitalClockDisplay
import com.example.ui.components.SettingsSheet

@Composable
fun ClockScreen(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val timeData by viewModel.timeData.collectAsStateWithLifecycle()
    val batteryInfo by viewModel.batteryInfo.collectAsStateWithLifecycle()
    val burnInOffset by viewModel.burnInOffset.collectAsStateWithLifecycle()
    val areControlsVisible by viewModel.areControlsVisible.collectAsStateWithLifecycle()
    val isSettingsSheetOpen by viewModel.isSettingsSheetOpen.collectAsStateWithLifecycle()

    val isNightMode = remember(settings, timeData.currentHour) {
        viewModel.isNightModeActive(settings, timeData.currentHour)
    }
    val effectiveTextColor = remember(settings, isNightMode) {
        viewModel.getEffectiveTextColor(settings, isNightMode)
    }
    val effectiveBrightness = remember(settings, isNightMode) {
        viewModel.getEffectiveScreenBrightness(settings, isNightMode)
    }

    LaunchedEffect(effectiveBrightness) {
        activity?.let { act ->
            val layoutParams = act.window.attributes
            layoutParams.screenBrightness = effectiveBrightness.coerceIn(0.01f, 1.0f)
            act.window.attributes = layoutParams
        }
    }

    DisposableEffect(settings.keepScreenOn) {
        val window = activity?.window
        if (settings.keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(settings.orientationPref) {
        activity?.let { act ->
            act.requestedOrientation = when (settings.orientationPref) {
                OrientationPreference.LANDSCAPE_SENSOR -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                OrientationPreference.LANDSCAPE_FIXED -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                OrientationPreference.SENSOR_ALL -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                OrientationPreference.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                viewModel.onScreenTapped()
            }
    ) {
        DigitalClockDisplay(
            timeData = timeData,
            settings = settings,
            burnInOffset = burnInOffset,
            targetColor = effectiveTextColor,
            isNightMode = isNightMode,
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = 12.dp)
        ) {
            BatteryIndicator(batteryInfo = batteryInfo)
        }

        AnimatedVisibility(
            visible = isNightMode && areControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0x66FF5252))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bedtime),
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Naktinis režimas",
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isNightMode) Color(0x44FF3333) else Color(0x33FFFFFF),
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.toggleManualNightMode() },
                        modifier = Modifier.testTag("quick_night_mode_button")
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isNightMode) R.drawable.ic_sunny else R.drawable.ic_bedtime
                            ),
                            contentDescription = if (isNightMode) "Išjungti naktinį režimą" else "Įjungti naktinį režimą",
                            tint = if (isNightMode) Color(0xFFFF5252) else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0x33FFFFFF),
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.openSettings() },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Nustatymai",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(start = 16.dp, bottom = 12.dp)
        ) {
            Text(
                text = "Palieskite ekraną, kad paslėptumėte mygtukus",
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 11.sp
            )
        }

        if (isSettingsSheetOpen) {
            SettingsSheet(
                settings = settings,
                onDismiss = { viewModel.closeSettings() },
                onColorSelected = { viewModel.updateTextColor(it) },
                onFontSelected = { viewModel.updateFont(it) },
                onToggle24Hour = { viewModel.toggle24Hour(it) },
                onToggleSeconds = { viewModel.toggleSeconds(it) },
                onToggleDate = { viewModel.toggleDate(it) },
                onToggleKeepScreenOn = { viewModel.toggleKeepScreenOn(it) },
                onToggleAutoNightMode = { viewModel.toggleAutoNightMode(it) },
                onUpdateNightHours = { start, end -> viewModel.updateNightHours(start, end) },
                onUpdateNormalBrightness = { viewModel.updateNormalBrightness(it) },
                onUpdateNightBrightness = { viewModel.updateNightBrightness(it) },
                onPersistBrightness = { viewModel.persistBrightnessChanges() },
                onToggleNightRedShift = { viewModel.toggleNightRedShift(it) },
                onToggleBurnInProtection = { viewModel.toggleBurnInProtection(it) },
                onUpdateOrientation = { viewModel.updateOrientation(it) }
            )
        }
    }
}
