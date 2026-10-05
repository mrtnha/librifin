package io.github.mrtnha.librifin.ui.reader

import androidx.compose.ui.graphics.Color

/**
 * The page colors of the reader, in the order the appearance sheet shows them: from darkest to brightest.
 * No pure black or white: pure white text on black glows and blurs, and pure white paper glares.
 * The app bar, page slider and appearance sheet are always dark, in a [bars] color that stands apart from the page.
 */
enum class ReaderTheme(val background: Color, val text: Color, val bars: Color, val isDark: Boolean) {
    /** The default, like the rest of the app. Contrast about 13:1. Bars a lighter gray than the page. */
    DARK(background = Color(0xFF121212), text = Color(0xFFDADADA), bars = Color(0xFF2C2C2E), isDark = true),

    /** Softer than dark for a dim room. Contrast about 7.4:1. Bars near black. */
    GRAY(background = Color(0xFF3C3C3E), text = Color(0xFFD4D4D4), bars = Color(0xFF1A1A1A), isDark = true),

    /** A barely warm paper white. Contrast about 15:1. Bars near black. */
    LIGHT(background = Color(0xFFF8F6F1), text = Color(0xFF1E1E1E), bars = Color(0xFF1A1A1A), isDark = false),
}
