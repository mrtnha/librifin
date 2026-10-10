package io.github.mrtnha.librifin.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals

class ScrubberTest {

    // 50 rows of 300 px (20 px spacing included), 32 px padding, a 1000 px window:
    // 50 * 300 - 20 + 32 - 1000 = 14012 px to scroll.
    private val map = GridScrollMap.of(rows = 50, rowPx = 300, spacingPx = 20, paddingPx = 32, viewportPx = 1000)

    @Test
    fun rangeIsTheContentMinusTheWindow() {
        assertEquals(14012, map.rangePx)
    }

    @Test
    fun topIsZero() {
        assertEquals(0f, map.fraction(firstRow = 0, rowOffsetPx = 0))
    }

    @Test
    fun endIsOne() {
        // 14012 px = 46 rows and 212 px.
        assertEquals(1f, map.fraction(firstRow = 46, rowOffsetPx = 212))
    }

    @Test
    fun halfwayIsAboutHalf() {
        assertEquals(0.5f, map.fraction(firstRow = 23, rowOffsetPx = 106), absoluteTolerance = 0.001f)
    }

    @Test
    fun aRowFoundByFractionGivesThatFractionBack() {
        for (fraction in listOf(0f, 0.1f, 0.37f, 0.5f, 0.82f, 1f)) {
            val (row, offset) = map.rowAt(fraction)
            assertEquals(fraction, map.fraction(row, offset), absoluteTolerance = 0.001f)
        }
    }

    @Test
    fun neverPastTheLastRow() {
        // A half-filled last row: 7 books in 2 columns are 4 rows. An overestimated window can't push past them.
        val short = GridScrollMap(rows = 4, rowPx = 300, rangePx = 5000)
        assertEquals(3 to 0, short.rowAt(1f))
    }

    @Test
    fun emptyOrUnmeasuredGridStaysAtTheTop() {
        val empty = GridScrollMap.of(rows = 0, rowPx = 300, spacingPx = 20, paddingPx = 32, viewportPx = 1000)
        assertEquals(0f, empty.fraction(0, 0))
        assertEquals(0 to 0, empty.rowAt(0.5f))
        val unmeasured = GridScrollMap.of(rows = 50, rowPx = 0, spacingPx = 20, paddingPx = 32, viewportPx = 1000)
        assertEquals(0f, unmeasured.fraction(10, 0))
        assertEquals(0 to 0, unmeasured.rowAt(0.5f))
    }
}
