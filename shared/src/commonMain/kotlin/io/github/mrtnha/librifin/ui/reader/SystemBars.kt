package io.github.mrtnha.librifin.ui.reader

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable

/**
 * Shows or hides the status and navigation bars while this is in the composition.
 * They are shown again when it leaves.
 */
@Composable
expect fun SystemBarsVisible(visible: Boolean)

/** The space the status and navigation bars take, also while they are hidden. */
@Composable
expect fun systemBarsIgnoringVisibility(): WindowInsets
