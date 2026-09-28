package io.github.mrtnha.librifin.ui.reader

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable

// Nothing to hide yet: there is no reader on iOS.
@Composable
actual fun SystemBarsVisible(visible: Boolean) = Unit

// The bars are never hidden on iOS yet.
@Composable
actual fun systemBarsIgnoringVisibility(): WindowInsets = WindowInsets.safeDrawing
