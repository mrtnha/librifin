package io.github.mrtnha.librifin.ui.reader

import androidx.compose.ui.graphics.Color

/**
 * The page colors of the reader, in the order the appearance sheet shows them, in pairs with the lighter page
 * first: three neutral pairs (dark and black, gray and charcoal, light and pewter), then five colors, each as a light
 * page and a dark one.
 * No pure white: white text glows and blurs on a dark page, and white paper glares.
 * The app bar, page slider and appearance sheet are always dark, in a [bars] color that stands apart from the page.
 */
enum class ReaderTheme(val background: Color, val text: Color, val bars: Color, val isDark: Boolean) {
    /**
     * The default, in the app's dark colors (ui/theme/Theme.kt): its background, text, and dialogs and sheets as the
     * bars. Suits every kind of screen, where black only pays off on OLED. Contrast about 14.4:1.
     */
    DARK(background = Color(0xFF0F1417), text = Color(0xFFDFE3E7), bars = Color(0xFF262B2E), isDark = true),

    /** Pure black: an OLED screen switches those pixels off. Contrast about 15:1. Bars a lighter gray than the page. */
    BLACK(background = Color(0xFF000000), text = Color(0xFFDADADA), bars = Color(0xFF2C2C2E), isDark = true),

    /**
     * The lightest of the dark pages, for a dim room: as light as a gray page gets while its text keeps about 7:1
     * without turning near white. Contrast about 7.2:1. Bars near black.
     */
    GRAY(background = Color(0xFF4B4B4D), text = Color(0xFFEAEAEA), bars = Color(0xFF1A1A1A), isDark = true),

    /** A dark gray, softer than dark. Contrast about 9.4:1. Bars near black. */
    CHARCOAL(background = Color(0xFF2C2C2E), text = Color(0xFFD4D4D4), bars = Color(0xFF1A1A1A), isDark = true),

    /** A barely warm paper white. Contrast about 15:1. Bars near black. */
    LIGHT(background = Color(0xFFF8F6F1), text = Color(0xFF1E1E1E), bars = Color(0xFF1A1A1A), isDark = false),

    /**
     * Light dimmed to a light gray, like an e-ink screen: light's tint and ink without its glare.
     * Contrast about 10.9:1. Bars near black.
     */
    PEWTER(background = Color(0xFFD3D1CD), text = Color(0xFF1E1E1E), bars = Color(0xFF1A1A1A), isDark = false),

    // Five colors, each as a light page and a dark one, with text in a shade of the same color. The dark pages are
    // lighter than dark and darker than gray.

    /** Pale green paper with dark green ink. Contrast about 10.6:1. Bars near black. */
    SAGE(background = Color(0xFFDFEDD9), text = Color(0xFF273524), bars = Color(0xFF1A1A1A), isDark = false),

    /** Dark green with pale green text. Contrast about 11.3:1. Bars a lighter green than the page. */
    FOREST(background = Color(0xFF18251C), text = Color(0xFFD1DDCE), bars = Color(0xFF2D3931), isDark = true),

    /** Pale rose paper with dark red-brown ink. Contrast about 10.8:1. Bars near black. */
    ROSE(background = Color(0xFFF9E1E1), text = Color(0xFF422827), bars = Color(0xFF1A1A1A), isDark = false),

    /** Dark wine red with pale rose text. Contrast about 11.5:1. Bars a lighter red than the page. */
    WINE(background = Color(0xFF2F191B), text = Color(0xFFE9D3D2), bars = Color(0xFF442F30), isDark = true),

    /** Pale blue paper with navy ink. Contrast about 10.8:1. Bars near black. */
    SKY(background = Color(0xFFDBEAF8), text = Color(0xFF223144), bars = Color(0xFF1A1A1A), isDark = false),

    /** Dark navy with pale blue-gray text. Contrast about 11.6:1. Bars a lighter blue than the page. */
    MIDNIGHT(background = Color(0xFF15202D), text = Color(0xFFD0DAE6), bars = Color(0xFF2A3543), isDark = true),

    /** Brown paper with dark brown ink, warm like a printed book. Contrast about 9.6:1. Bars near black. */
    KRAFT(background = Color(0xFFE0CAAE), text = Color(0xFF332214), bars = Color(0xFF1A1A1A), isDark = false),

    /** Dark brown with cream text. Contrast about 11.3:1. Bars a lighter brown than the page. */
    COFFEE(background = Color(0xFF2A1F17), text = Color(0xFFE4D7C3), bars = Color(0xFF3D332B), isDark = true),

    /** Pale lavender paper with dark purple ink. Contrast about 10.8:1. Bars near black. */
    LAVENDER(background = Color(0xFFECE4F6), text = Color(0xFF362A43), bars = Color(0xFF1A1A1A), isDark = false),

    /** Dark plum with pale lilac text. Contrast about 11.5:1. Bars a lighter purple than the page. */
    PLUM(background = Color(0xFF261C2D), text = Color(0xFFDED5E6), bars = Color(0xFF3A3141), isDark = true),
}

/**
 * The theme saved as [saved] (its name), or [ReaderTheme.DARK] if none is saved. While nothing is saved yet, the
 * theme saved by versions before the dark one ([savedBefore]) counts: they called the black one "DARK".
 */
fun readerThemeOf(saved: String?, savedBefore: String?): ReaderTheme =
    themeNamed(saved) ?: if (savedBefore == "DARK") ReaderTheme.BLACK else themeNamed(savedBefore) ?: ReaderTheme.DARK

/** The theme saved as [name]. The sepia page, saved as "SEPIA", is gone: Kraft, the other warm paper, takes over. */
private fun themeNamed(name: String?): ReaderTheme? =
    if (name == "SEPIA") ReaderTheme.KRAFT else ReaderTheme.entries.find { it.name == name }
