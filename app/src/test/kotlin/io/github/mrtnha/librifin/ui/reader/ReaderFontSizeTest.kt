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
    fun sizesNotOfferedAreTheBooksOwnSize() {
        assertEquals(100, readerFontSizeOf("105"))
        assertEquals(100, readerFontSizeOf("50"))
        assertEquals(100, readerFontSizeOf("300"))
    }
}
