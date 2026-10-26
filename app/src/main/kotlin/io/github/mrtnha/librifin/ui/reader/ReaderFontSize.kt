package io.github.mrtnha.librifin.ui.reader

import kotlin.math.abs

/**
 * The text sizes the reader offers, in percent of the book's own, smallest first: steps of 10 up to 200 %, then
 * steps of 20. Up there a step of 10 would change the text by only 4 or 5 %, hard to see, while the large sizes are
 * the ones older readers and readers with low vision use. A step of 20 changes it by 8 to 10 %.
 * Readium allows 10–500 %; below 70 % text on a phone is too small to read, and at 260 % only two or three words
 * fit on a line.
 */
val READER_FONT_SIZES = listOf(70, 80, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190, 200, 220, 240, 260)

/** The book's own size. */
const val DEFAULT_FONT_SIZE = 100

/**
 * The size saved as [saved], or [DEFAULT_FONT_SIZE] if none is saved. A size that's no longer offered, like the 210,
 * 230 and 250 % of earlier versions, becomes the nearest one offered, the larger of two as near: text a bit too large
 * is easier to read than text a bit too small.
 */
fun readerFontSizeOf(saved: String?): Int {
    val size = saved?.toIntOrNull() ?: return DEFAULT_FONT_SIZE
    // The first of the nearest: searched from the largest down, that's the larger of two.
    return READER_FONT_SIZES.asReversed().minBy { abs(it - size) }
}
