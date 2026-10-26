package io.github.mrtnha.librifin.ui.reader

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import io.github.mrtnha.librifin.ui.theme.readerSheetColorScheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReaderThemeTest {

    @Test
    fun savedThemeIsReadBack() {
        ReaderTheme.entries.forEach { assertEquals(it, readerThemeOf(it.name)) }
    }

    @Test
    fun nothingSavedIsDark() {
        assertEquals(ReaderTheme.DARK, readerThemeOf(null))
        assertEquals(ReaderTheme.DARK, readerThemeOf("abc"))
    }

    @Test
    fun everySliderStandsOutFromItsBars() {
        // 3:1 is the least contrast a control needs to be seen (WCAG).
        ReaderTheme.entries.forEach {
            val contrast = contrast(it.slider, it.bars)
            assertTrue(contrast >= 3f, "${it.name}: the slider has $contrast:1 against the bars")
        }
    }

    @Test
    fun everySelectedLabelInTheSheetIsReadable() {
        // 4.5:1 is the least contrast text needs to be read (WCAG).
        ReaderTheme.entries.forEach {
            val colors = readerSheetColorScheme(it.bars, it.slider)
            val contrast = contrast(colors.onSecondaryContainer, colors.secondaryContainer)
            assertTrue(contrast >= 4.5f, "${it.name}: a selected label has $contrast:1 against its selection")
        }
    }

    /** The contrast ratio of two colors, as WCAG defines it: 1 for the same color, 21 for black and white. */
    private fun contrast(a: Color, b: Color): Float {
        val (lighter, darker) = listOf(a.luminance(), b.luminance()).sortedDescending()
        return (lighter + 0.05f) / (darker + 0.05f)
    }
}
