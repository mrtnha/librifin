package io.github.mrtnha.librifin.ui.reader

import kotlin.test.Test
import kotlin.test.assertEquals

class ReaderThemeTest {

    @Test
    fun savedThemeIsReadBack() {
        ReaderTheme.entries.forEach { assertEquals(it, readerThemeOf(saved = it.name, savedBefore = null)) }
    }

    @Test
    fun nothingSavedIsDark() {
        assertEquals(ReaderTheme.DARK, readerThemeOf(saved = null, savedBefore = null))
    }

    @Test
    fun blackSavedBeforeTheDarkThemeStaysBlack() {
        assertEquals(ReaderTheme.BLACK, readerThemeOf(saved = null, savedBefore = "DARK"))
    }

    @Test
    fun otherThemesSavedBeforeCarryOver() {
        assertEquals(ReaderTheme.SAGE, readerThemeOf(saved = null, savedBefore = "SAGE"))
        assertEquals(ReaderTheme.PLUM, readerThemeOf(saved = null, savedBefore = "PLUM"))
    }

    @Test
    fun aThemeSavedNowWinsOverTheOneSavedBefore() {
        assertEquals(ReaderTheme.DARK, readerThemeOf(saved = "DARK", savedBefore = "DARK"))
        assertEquals(ReaderTheme.LIGHT, readerThemeOf(saved = "LIGHT", savedBefore = "SAGE"))
    }

    @Test
    fun sepiaContinuesAsKraft() {
        assertEquals(ReaderTheme.KRAFT, readerThemeOf(saved = "SEPIA", savedBefore = null))
        assertEquals(ReaderTheme.KRAFT, readerThemeOf(saved = null, savedBefore = "SEPIA"))
    }
}
