package io.github.mrtnha.librifin.ui.reader

import kotlin.test.Test
import kotlin.test.assertEquals

class ReaderPageMarginsTest {

    @Test
    fun savedMarginsAreReadBack() {
        READER_PAGE_MARGINS.forEach { assertEquals(it, readerPageMarginsOf(it.toString())) }
    }

    @Test
    fun nothingSavedIsReadiumsUsualMargins() {
        assertEquals(100, readerPageMarginsOf(null))
        assertEquals(100, readerPageMarginsOf("abc"))
    }

    @Test
    fun marginsNotOfferedBecomeTheNearest() {
        assertEquals(60, readerPageMarginsOf("55"))
        assertEquals(120, readerPageMarginsOf("125"))
    }

    @Test
    fun marginsBeyondTheEndsBecomeTheEnds() {
        assertEquals(0, readerPageMarginsOf("-20"))
        assertEquals(200, readerPageMarginsOf("400"))
    }
}
