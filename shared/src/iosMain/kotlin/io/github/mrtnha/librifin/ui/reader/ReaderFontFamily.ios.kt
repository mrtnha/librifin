package io.github.mrtnha.librifin.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

// The font files are bundled for Android only so far; they come to iOS with the reader.
@Composable
actual fun rememberFontFamily(font: ReaderFont): FontFamily = FontFamily.Default
