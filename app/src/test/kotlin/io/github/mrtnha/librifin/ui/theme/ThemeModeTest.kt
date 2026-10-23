package io.github.mrtnha.librifin.ui.theme

import android.app.UiModeManager
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeModeTest {

    @Test
    fun fixedModesIgnoreThePhone() {
        for (phoneIsDark in listOf(false, true)) {
            assertEquals(false, ThemeMode.LIGHT.isDark(phoneIsDark))
            assertEquals(false, ThemeMode.LIGHT.isBlack(phoneIsDark))
            assertEquals(true, ThemeMode.DARK.isDark(phoneIsDark))
            assertEquals(false, ThemeMode.DARK.isBlack(phoneIsDark))
            assertEquals(true, ThemeMode.BLACK.isDark(phoneIsDark))
            assertEquals(true, ThemeMode.BLACK.isBlack(phoneIsDark))
        }
    }

    @Test
    fun systemModesFollowThePhone() {
        assertEquals(false, ThemeMode.SYSTEM.isDark(phoneIsDark = false))
        assertEquals(true, ThemeMode.SYSTEM.isDark(phoneIsDark = true))
        assertEquals(false, ThemeMode.SYSTEM.isBlack(phoneIsDark = true))

        assertEquals(false, ThemeMode.SYSTEM_BLACK.isDark(phoneIsDark = false))
        assertEquals(false, ThemeMode.SYSTEM_BLACK.isBlack(phoneIsDark = false))
        assertEquals(true, ThemeMode.SYSTEM_BLACK.isDark(phoneIsDark = true))
        assertEquals(true, ThemeMode.SYSTEM_BLACK.isBlack(phoneIsDark = true))
    }

    @Test
    fun androidLearnsTheNightMode() {
        assertEquals(UiModeManager.MODE_NIGHT_NO, ThemeMode.LIGHT.nightMode)
        assertEquals(UiModeManager.MODE_NIGHT_YES, ThemeMode.DARK.nightMode)
        assertEquals(UiModeManager.MODE_NIGHT_YES, ThemeMode.BLACK.nightMode)
        assertEquals(UiModeManager.MODE_NIGHT_AUTO, ThemeMode.SYSTEM.nightMode)
        assertEquals(UiModeManager.MODE_NIGHT_AUTO, ThemeMode.SYSTEM_BLACK.nightMode)
    }

    @Test
    fun savedModeIsReadBack() {
        ThemeMode.entries.forEach { assertEquals(it, themeModeOf(it.name)) }
    }

    @Test
    fun nothingOrUnknownSavedFollowsThePhone() {
        assertEquals(ThemeMode.SYSTEM, themeModeOf(null))
        assertEquals(ThemeMode.SYSTEM, themeModeOf("SEPIA"))
    }
}
