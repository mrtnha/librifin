package io.github.mrtnha.librifin.ui.reader

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

    @Test
    fun stepsGoToTheNeighbors() {
        assertEquals(90, smallerFontSize(100))
        assertEquals(110, largerFontSize(100))
        assertEquals(190, smallerFontSize(200))
        assertEquals(220, largerFontSize(200))
        assertEquals(200, smallerFontSize(220))
        assertEquals(240, largerFontSize(220))
    }

    @Test
    fun noStepsBeyondTheEnds() {
        assertNull(smallerFontSize(70))
        assertNull(largerFontSize(260))
    }
}
