package io.github.mrtnha.librifin.ui.reader

/**
 * The font weights the reader offers, from 200 (extra light) to 800 (extra bold) in steps of 100, with null for the
 * book's own. Books almost always set the normal weight, 400, so null takes its place. Headings and bold words keep
 * their own weights. A font shows only the weights it has: the variable ones show their range, Merriweather and Courier
 * Prime their regular face up to 500 and their bold one from 600.
 */
val READER_FONT_WEIGHTS: List<Int?> = listOf(200, 300, null, 500, 600, 700, 800)

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
