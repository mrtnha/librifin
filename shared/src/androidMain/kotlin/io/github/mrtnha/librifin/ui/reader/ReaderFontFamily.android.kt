package io.github.mrtnha.librifin.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

/** Where the bundled font files are in the app's assets, for the reader and the app's own text alike. */
internal fun FontFace.assetPath(): String = "fonts/$path"

/** The pattern for [assetPath]s, to let the reader load them. */
internal const val FONT_ASSETS = "fonts/.*"

@Composable
actual fun rememberFontFamily(font: ReaderFont): FontFamily {
    val assets = LocalContext.current.assets
    return remember(font) {
        // The regular face is enough for a font's name; for variable fonts, Compose picks the normal weight.
        val regular = font.faces.firstOrNull { !it.isItalic && 400 in it.weights }
        if (regular == null) FontFamily.Default else FontFamily(Font(regular.assetPath(), assets))
    }
}
