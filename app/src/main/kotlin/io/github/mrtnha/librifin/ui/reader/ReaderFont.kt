package io.github.mrtnha.librifin.ui.reader

/**
 * The fonts the reader offers, in the order the appearance sheet shows them. [ORIGINAL] keeps the book's
 * own fonts. The others are bundled with the app (SIL Open Font License, each with its license file),
 * so choosing one never downloads anything. Each has a real italic and bold, which novels need.
 */
enum class ReaderFont(val label: String, val faces: List<FontFace>) {
    ORIGINAL("Original", emptyList()),

    // Serifs
    /** Made for reading long texts on screens. */
    LITERATA("Literata", variable("literata/Literata.ttf", "literata/Literata-Italic.ttf", 200..900)),

    /** A classic book typeface, like a printed novel. */
    EB_GARAMOND(
        "EB Garamond",
        variable("eb-garamond/EBGaramond.ttf", "eb-garamond/EBGaramond-Italic.ttf", 400..800),
    ),

    /** Warm and slightly calligraphic. */
    LORA("Lora", variable("lora/Lora.ttf", "lora/Lora-Italic.ttf", 400..700)),

    /** Lively and old-style. */
    ALEGREYA("Alegreya", variable("alegreya/Alegreya.ttf", "alegreya/Alegreya-Italic.ttf", 400..900)),

    /**
     * Sturdy, with large lowercase letters: stays readable when small and on dark pages. Static files:
     * its variable ones are 9 MB, because they also vary in width and optical size.
     */
    MERRIWEATHER("Merriweather", static("merriweather/Merriweather")),

    /** A slab serif: crisp and modern. */
    BITTER("Bitter", variable("bitter/Bitter.ttf", "bitter/Bitter-Italic.ttf", 100..900)),

    // Sans-serifs
    /** Clean and neutral, made for long texts. */
    SOURCE_SANS(
        "Source Sans",
        variable("source-sans/SourceSans3.ttf", "source-sans/SourceSans3-Italic.ttf", 200..900),
    ),

    /** Soft and friendly, with rounded details. */
    NUNITO_SANS(
        "Nunito Sans",
        variable("nunito-sans/NunitoSans.ttf", "nunito-sans/NunitoSans-Italic.ttf", 200..1000),
    ),

    // Special purposes
    /** Letters that look alike (l, I, 1; O, 0) are easy to tell apart: for low vision and dyslexia. */
    ATKINSON_HYPERLEGIBLE(
        "Atkinson Hyperlegible",
        variable(
            "atkinson-hyperlegible/AtkinsonHyperlegibleNext.ttf",
            "atkinson-hyperlegible/AtkinsonHyperlegibleNext-Italic.ttf",
            200..800,
        ),
    ),

    /** A typewriter font, for a manuscript feel. */
    COURIER_PRIME("Courier Prime", static("courier-prime/CourierPrime")),
}

/**
 * One file of a [ReaderFont]: its [path] among the bundled font files, whether it's the italic,
 * and the [weights] it covers (a range for variable fonts, a single weight for static ones).
 */
class FontFace(val path: String, val isItalic: Boolean, val weights: IntRange)

/** A variable font: one file for all weights, one for their italics. */
private fun variable(regular: String, italic: String, weights: IntRange) =
    listOf(FontFace(regular, isItalic = false, weights), FontFace(italic, isItalic = true, weights))

/** A static font: a file each for regular, italic, bold and bold italic, named "<prefix>-Regular.ttf" etc. */
private fun static(prefix: String) = listOf(
    FontFace("$prefix-Regular.ttf", isItalic = false, weights = 400..400),
    FontFace("$prefix-Italic.ttf", isItalic = true, weights = 400..400),
    FontFace("$prefix-Bold.ttf", isItalic = false, weights = 700..700),
    FontFace("$prefix-BoldItalic.ttf", isItalic = true, weights = 700..700),
)
