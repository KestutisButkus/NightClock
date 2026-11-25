package com.example

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.data.ClockSettingsRepository
import com.example.model.ClockFont
import com.example.model.ClockSettings
import com.example.ui.theme.ClockFonts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Naktinis Laikrodis", appName)
  }

  @Test
  fun `verify settings repository default values and updates`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = ClockSettingsRepository(context)
    val initialSettings = repository.settings.value

    assertTrue(initialSettings.keepScreenOn)
    assertTrue(initialSettings.autoNightModeEnabled)

    val updated = initialSettings.copy(
      textColor = Color(0xFFFF9100),
      selectedFont = ClockFont.SHARE_TECH_MONO,
      showSeconds = true
    )
    repository.updateSettings(updated)

    val savedSettings = repository.settings.value
    assertEquals(ClockFont.SHARE_TECH_MONO, savedSettings.selectedFont)
    assertTrue(savedSettings.showSeconds)
  }

  @Test
  fun `verify font families resolve`() {
    for (font in ClockFont.entries) {
      val family = ClockFonts.getFontFamily(font)
      assertNotNull(family)
    }
  }

  @Test
  fun `verify parseHexColor handles FC2407 and other formats`() {
    val color1 = com.example.ui.components.parseHexColor("#FC2407")
    assertNotNull(color1)
    assertEquals(0xFC, (color1!!.red * 255).toInt())
    assertEquals(0x24, (color1.green * 255).toInt())
    assertEquals(0x07, (color1.blue * 255).toInt())

    val color2 = com.example.ui.components.parseHexColor("FC2407")
    assertNotNull(color2)
    assertEquals(color1, color2)

    val invalid = com.example.ui.components.parseHexColor("XYZ123")
    org.junit.Assert.assertNull(invalid)
  }
}
