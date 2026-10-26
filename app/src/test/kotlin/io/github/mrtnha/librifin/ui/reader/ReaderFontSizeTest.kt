package io.github.mrtnha.librifin.ui.reader

import kotlin.test.Test
import kotlin.test.assertEquals

class ReaderFontSizeTest {

    @Test
    fun savedSizeIsReadBack() {
        READER_FONT_SIZES.forEach { assertEquals(it, readerFontSizeOf(it.toString())) }
    }

    @Test
    fun nothingSavedIsTheBooksOwnSize() {
        assertEquals(100, readerFontSizeOf(null))
        assertEquals(100, readerFontSizeOf("abc"))
    }

    @Test
    fun sizesOfEarlierVersionsBecomeTheLargerNeighbor() {
        assertEquals(220, readerFontSizeOf("210"))
        assertEquals(240, readerFontSizeOf("230"))
        assertEquals(260, readerFontSizeOf("250"))
    }

    @Test
    fun sizesBeyondTheEndsBecomeTheEnds() {
        assertEquals(70, readerFontSizeOf("50"))
        assertEquals(260, readerFontSizeOf("300"))
    }
}
