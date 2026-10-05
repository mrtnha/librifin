package io.github.mrtnha.librifin.ui.reader

import androidx.compose.runtime.Composable

/**
 * Gets the book renderer ready ahead of time while this is in the composition, so the first book
 * after an app start opens faster. Only the first call per app start does anything.
 */
@Composable
expect fun PrepareReader()
