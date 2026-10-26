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
    fun marginsNotOfferedAreReadiumsUsualOnes() {
        assertEquals(100, readerPageMarginsOf("55"))
        assertEquals(100, readerPageMarginsOf("-20"))
        assertEquals(100, readerPageMarginsOf("400"))
    }
}
