package io.github.mrtnha.librifin.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.io.files.Path

/**
 * Renders the EPUB in [file] paginated, starting at [initialLocator] (a value from
 * [onPositionChanged]), else at [initialProgress] through the whole book (0..1), else at the beginning,
 * in the colors of [theme].
 * When [jumpToProgress] is set, it goes there and calls [onJumped]. [onReachedEnd] is called when
 * the last page of the book is shown. [onPagesLoaded] gets the book's pages once it's open.
 * Taps on the left and right edges and swipes turn pages; other taps on the page call [onCenterTap].
 * [onPositionChanged] gets the exact position (a string only this renderer understands), the
 * progress through the whole book (0..1) and the page (1-based). If the file can't be opened as a
 * book, [onOpenFailed] gets a message for the user.
 */
@Composable
expect fun EpubView(
    file: Path,
    initialLocator: String?,
    initialProgress: Double?,
    theme: ReaderTheme,
    jumpToProgress: Double?,
    onJumped: () -> Unit,
    onReachedEnd: () -> Unit,
    onPagesLoaded: (pages: List<BookPage>) -> Unit,
    onPositionChanged: (locator: String, progress: Double, page: Int?) -> Unit,
    onCenterTap: () -> Unit,
    onOpenFailed: (message: String) -> Unit,
    modifier: Modifier = Modifier,
)

/**
 * One page of a book: a fixed part of about 1,000 characters (Readium's "positions"), so the page
 * count doesn't depend on screen or font size. [progress] is where it starts (0..1), [chapter] the
 * title of the chapter it's in, if known.
 */
data class BookPage(val progress: Double, val chapter: String?)
