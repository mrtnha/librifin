package io.github.mrtnha.librifin.ui.reader

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReaderLineHeightTest {

    @Test
    fun savedLineHeightIsReadBack() {
        READER_LINE_HEIGHTS.forEach { assertEquals(it, readerLineHeightOf(lineHeightForSaving(it))) }
    }

    @Test
    fun nothingSavedIsTheBooksOwn() {
        assertNull(readerLineHeightOf(null))
        assertNull(readerLineHeightOf("abc"))
    }

    @Test
    fun lineHeightsBeyondTheEndsBecomeTheEnds() {
        assertEquals(10, readerLineHeightOf("8"))
        assertEquals(20, readerLineHeightOf("25"))
    }

    @Test
    fun labels() {
        assertEquals("Original", lineHeightLabel(null))
        assertEquals("1.0", lineHeightLabel(10))
        assertEquals("1.5", lineHeightLabel(15))
        assertEquals("2.0", lineHeightLabel(20))
    }
}
