package io.github.mrtnha.librifin.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.io.files.Path

/**
 * Renders the EPUB in [file] paginated. Taps on the left and right edges and swipes turn pages;
 * other taps on the page call [onCenterTap]. If the file can't be opened as a book, [onOpenFailed]
 * gets a message for the user.
 */
@Composable
expect fun EpubView(
    file: Path,
    onCenterTap: () -> Unit,
    onOpenFailed: (message: String) -> Unit,
    modifier: Modifier = Modifier,
)
