package io.github.mrtnha.librifin.ui.reader

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable

/**
 * Shows or hides the status and navigation bars while this is in the composition, with light or dark
 * icons to suit the background behind them ([darkBackground]).
 * They are shown again, with the app's usual icons, when it leaves.
 */
@Composable
expect fun SystemBarsVisible(visible: Boolean, darkBackground: Boolean)

/** The space the status and navigation bars take, also while they are hidden. */
@Composable
expect fun systemBarsIgnoringVisibility(): WindowInsets
