package io.github.mrtnha.librifin.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.io.files.Path

/**
 * Renders the EPUB in [file] paginated, starting at [initialLocator] (a value from
 * [onPositionChanged]), else at [initialProgress] through the whole book (0..1), else at the beginning.
 * When [jumpToProgress] is set, it goes there and calls [onJumped]. Taps on the left and right edges and swipes turn pages;
 * other taps on the page call [onCenterTap]. [onPositionChanged] gets the exact position (a string
 * only this renderer understands) and the progress through the whole book (0..1). If the file can't
 * be opened as a book, [onOpenFailed] gets a message for the user.
 */
@Composable
expect fun EpubView(
    file: Path,
    initialLocator: String?,
    initialProgress: Double?,
    jumpToProgress: Double?,
    onJumped: () -> Unit,
    onPositionChanged: (locator: String, progress: Double) -> Unit,
    onCenterTap: () -> Unit,
    onOpenFailed: (message: String) -> Unit,
    modifier: Modifier = Modifier,
)
