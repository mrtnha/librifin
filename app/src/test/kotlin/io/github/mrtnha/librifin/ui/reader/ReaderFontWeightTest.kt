package io.github.mrtnha.librifin.ui.reader

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReaderFontWeightTest {

    @Test
    fun savedFontWeightIsReadBack() {
        READER_FONT_WEIGHTS.forEach { assertEquals(it, readerFontWeightOf(fontWeightForSaving(it))) }
    }

    @Test
    fun nothingSavedIsTheBooksOwn() {
        assertNull(readerFontWeightOf(null))
        assertNull(readerFontWeightOf("abc"))
    }

    @Test
    fun fontWeightsNotOfferedAreTheBooksOwn() {
        assertNull(readerFontWeightOf("400"))
        assertNull(readerFontWeightOf("250"))
        assertNull(readerFontWeightOf("900"))
    }

    @Test
    fun labels() {
        assertEquals("Original", fontWeightLabel(null))
        assertEquals("Extra light", fontWeightLabel(200))
        assertEquals("Semibold", fontWeightLabel(600))
        assertEquals("Extra bold", fontWeightLabel(800))
    }
}
