package io.github.mrtnha.librifin.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

/** [font] for the app's own text, e.g. to show a font's name in that font. The app's font for [ReaderFont.ORIGINAL]. */
@Composable
expect fun rememberFontFamily(font: ReaderFont): FontFamily
