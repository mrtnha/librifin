package io.github.mrtnha.librifin.ui.reader

import kotlin.math.abs

/**
 * The font weights the reader offers, from 200 (extra light) to 800 (extra bold) in steps of 100, with null for the
 * book's own. Books almost always set the normal weight, 400, so null takes its place. Headings and bold words keep
 * their own weights. Each font offers the ones it has ([fontWeights]).
 */
val READER_FONT_WEIGHTS: List<Int?> = listOf(200, 300, null, 500, 600, 700, 800)

/** The normal font weight: what the book's own (null) counts as, and what Readium's font weight is a factor of. */
const val NORMAL_FONT_WEIGHT = 400

/**
 * The [READER_FONT_WEIGHTS] this font has, so every step on the slider changes the page: the book's own and those its
 * files cover. All of them for the book's own fonts, as only the book knows which weights they have.
 */
val ReaderFont.fontWeights: List<Int?>
    get() = if (this == ReaderFont.ORIGINAL) {
        READER_FONT_WEIGHTS
    } else {
        READER_FONT_WEIGHTS.filter { weight -> weight == null || faces.any { weight in it.weights } }
    }

/**
 * [fontWeight] in this font: the nearest of its [fontWeights], which is also the one the page shows for a weight a
 * font doesn't have. So a weight picked in one font comes back in the next font that has it.
 */
fun ReaderFont.shownFontWeight(fontWeight: Int?): Int? =
    fontWeights.minBy { abs((it ?: NORMAL_FONT_WEIGHT) - (fontWeight ?: NORMAL_FONT_WEIGHT)) }

/** The font weight saved as [saved], or the book's own (null) if none or one that isn't offered is saved. */
fun readerFontWeightOf(saved: String?): Int? = saved?.toIntOrNull()?.takeIf { it in READER_FONT_WEIGHTS }

/** How [fontWeight] is saved, for [readerFontWeightOf]: a word for the book's own, as only text can be saved. */
fun fontWeightForSaving(fontWeight: Int?): String = fontWeight?.toString() ?: "original"

/** How [fontWeight] is shown: "Original" for the book's own, else by the weight's usual name, like "Semibold". */
fun fontWeightLabel(fontWeight: Int?): String = when (fontWeight) {
    null -> "Original"
    200 -> "Extra light"
    300 -> "Light"
    500 -> "Medium"
    600 -> "Semibold"
    700 -> "Bold"
    800 -> "Extra bold"
    else -> fontWeight.toString()
}
