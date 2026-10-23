package io.github.mrtnha.librifin.ui.reader

import kotlin.test.Test
import kotlin.test.assertEquals

class ReaderThemeTest {

    @Test
    fun savedThemeIsReadBack() {
        ReaderTheme.entries.forEach { assertEquals(it, readerThemeOf(saved = it.name, savedBefore = null)) }
    }

    @Test
    fun nothingSavedIsBlack() {
        assertEquals(ReaderTheme.BLACK, readerThemeOf(saved = null, savedBefore = null))
    }

    @Test
    fun blackSavedBeforeTheDarkThemeStaysBlack() {
        assertEquals(ReaderTheme.BLACK, readerThemeOf(saved = null, savedBefore = "DARK"))
    }

    @Test
    fun otherThemesSavedBeforeCarryOver() {
        assertEquals(ReaderTheme.SEPIA, readerThemeOf(saved = null, savedBefore = "SEPIA"))
        assertEquals(ReaderTheme.PLUM, readerThemeOf(saved = null, savedBefore = "PLUM"))
    }

    @Test
    fun aThemeSavedNowWinsOverTheOneSavedBefore() {
        assertEquals(ReaderTheme.DARK, readerThemeOf(saved = "DARK", savedBefore = "DARK"))
        assertEquals(ReaderTheme.LIGHT, readerThemeOf(saved = "LIGHT", savedBefore = "SEPIA"))
    }
}
