package io.github.mrtnha.librifin.ui.reader

/**
 * The line spacings the reader offers, in tenths of the text size (15 is 1.5 times it), tightest first, with null for
 * the book's own. Readium suggests 1.0 to 2.0. The book's own spacing differs from book to book: books often set 1.2
 * to 1.5, and without one Readium's comes to about 1.35 to 1.5, depending on the font. So it sits between 1.4 and
 * 1.5: from there, the next step to the right makes the lines looser and the one to the left tighter in most books.
 */
val READER_LINE_HEIGHTS: List<Int?> = listOf(10, 11, 12, 13, 14, null, 15, 16, 17, 18, 19, 20)

/** The line spacing saved as [saved], or the book's own (null) if none or one that isn't offered is saved. */
fun readerLineHeightOf(saved: String?): Int? = saved?.toIntOrNull()?.takeIf { it in READER_LINE_HEIGHTS }

/** How [lineHeight] is saved, for [readerLineHeightOf]: a word for the book's own, as only text can be saved. */
fun lineHeightForSaving(lineHeight: Int?): String = lineHeight?.toString() ?: "original"

/** How [lineHeight] is shown: "Original" for the book's own, else as a multiple of the text size, like "1.5". */
fun lineHeightLabel(lineHeight: Int?): String = lineHeight?.let { "${it / 10}.${it % 10}" } ?: "Original"
