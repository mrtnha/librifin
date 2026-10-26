package io.github.mrtnha.librifin.ui.reader

/**
 * The page margins the reader offers, in percent of Readium's usual ones, smallest first. Readium's usual margin is
 * 20 dp on each side of a phone's page (30 to 50 dp on screens from 560 dp wide), so on a phone each step adds 4 dp
 * on each side. At 0 the text goes to the screen's edges; at 200 % a phone 360 dp wide keeps 280 dp for it. Only the
 * left and right margins: Readium keeps the top and bottom ones fixed.
 */
val READER_PAGE_MARGINS = listOf(0, 20, 40, 60, 80, 100, 120, 140, 160, 180, 200)

/** Readium's usual margins. */
const val DEFAULT_PAGE_MARGINS = 100

/** The margins saved as [saved], or [DEFAULT_PAGE_MARGINS] if none or ones that aren't offered are saved. */
fun readerPageMarginsOf(saved: String?): Int =
    saved?.toIntOrNull()?.takeIf { it in READER_PAGE_MARGINS } ?: DEFAULT_PAGE_MARGINS
