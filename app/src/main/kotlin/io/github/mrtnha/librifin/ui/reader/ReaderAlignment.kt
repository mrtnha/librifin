package io.github.mrtnha.librifin.ui.reader

/** How the lines of the book's text are aligned, in the order the appearance sheet shows them. */
enum class ReaderAlignment {
    /** A straight left edge and a ragged right one, with even spaces between words. Mirrored in right-to-left books. */
    LEFT,

    /** Both edges straight, like a printed book. Words are hyphenated, so the spaces between them stay small. */
    JUSTIFIED,
}
