package io.github.mrtnha.librifin.ui.reader

import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    @Test
    fun everySliderStandsOutFromItsBars() {
        // 3:1 is the least contrast a control needs to be seen (WCAG).
        ReaderTheme.entries.forEach {
            val (lighter, darker) = listOf(it.slider.luminance(), it.bars.luminance()).sortedDescending()
            val contrast = (lighter + 0.05f) / (darker + 0.05f)
            assertTrue(contrast >= 3f, "${it.name}: the slider has $contrast:1 against the bars")
        }
    }
}
