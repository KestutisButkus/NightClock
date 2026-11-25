package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val PureBlackColorScheme =
  darkColorScheme(
    primary = Color(0xFF00E5FF),
    secondary = Color(0xFF00FF66),
    tertiary = Color(0xFFFF9100),
    background = Color.Black,
    surface = Color(0xFF121418),
    onBackground = Color.White,
    onSurface = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = PureBlackColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
