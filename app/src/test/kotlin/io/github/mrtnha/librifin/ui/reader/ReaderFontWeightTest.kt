package io.github.mrtnha.librifin.ui.reader

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
    fun theBooksOwnFontsOfferAllWeights() {
        assertEquals(READER_FONT_WEIGHTS, ReaderFont.ORIGINAL.fontWeights)
    }

    @Test
    fun fontsOfferTheWeightsTheyHave() {
        assertEquals(READER_FONT_WEIGHTS, ReaderFont.LITERATA.fontWeights)
        assertEquals(listOf(null, 500, 600, 700), ReaderFont.LORA.fontWeights)
        assertEquals(listOf(null, 500, 600, 700, 800), ReaderFont.EB_GARAMOND.fontWeights)
        assertEquals(listOf(null, 700), ReaderFont.MERRIWEATHER.fontWeights)
    }

    @Test
    fun everyFontHasTwoWeightsAtLeast() {
        ReaderFont.entries.forEach { assertTrue(it.fontWeights.size >= 2, it.name) }
    }

    @Test
    fun aFontShowsItsOwnWeightsAsTheyAre() {
        ReaderFont.entries.forEach { font ->
            font.fontWeights.forEach { assertEquals(it, font.shownFontWeight(it)) }
        }
    }

    @Test
    fun aFontShowsOtherWeightsAsTheNearestItHas() {
        assertEquals(700, ReaderFont.LORA.shownFontWeight(800))
        assertNull(ReaderFont.LORA.shownFontWeight(200))
        assertNull(ReaderFont.MERRIWEATHER.shownFontWeight(300))
        assertNull(ReaderFont.MERRIWEATHER.shownFontWeight(500))
        assertEquals(700, ReaderFont.MERRIWEATHER.shownFontWeight(600))
    }

    @Test
    fun labels() {
        assertEquals("Original", fontWeightLabel(null))
        assertEquals("Extra light", fontWeightLabel(200))
        assertEquals("Semibold", fontWeightLabel(600))
        assertEquals("Extra bold", fontWeightLabel(800))
    }
}
