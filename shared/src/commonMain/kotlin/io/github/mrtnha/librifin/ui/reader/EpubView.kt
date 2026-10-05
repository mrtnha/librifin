package io.github.mrtnha.librifin.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.io.files.Path

/**
 * Renders the EPUB in [file] paginated, starting at [initialLocator] (a value from
 * [EpubViewHost.onPositionChanged]), else at the beginning. [host] says how, and hears what happens.
 */
@Composable
expect fun EpubView(file: Path, initialLocator: String?, host: EpubViewHost, modifier: Modifier = Modifier)

/**
 * What [EpubView] shows and reports. The book is in the colors of [theme], with text at [fontSize] percent of
 * the book's own size, in [font].
 * When [jumpToProgress] is set, it goes there and calls [onJumped]. [onReachedEnd] is called when
 * the last page of the book is shown. [onPagesLoaded] gets the book's pages once it's open.
 * Taps on the left and right edges and swipes turn pages; other taps on the page call [onCenterTap].
 * [onPositionChanged] gets the exact position (a string only this renderer understands), the
 * progress through the whole book (0..1) and the page (1-based). If the file can't be opened as a
 * book, [onOpenFailed] gets a message for the user.
 *
 * While [searchedQuery] is set, the book is searched for it, and [onSearchResults] gets the results found
 * so far after each chapter, with isDone once the whole book is searched. When [showSearchResult] is set,
 * it goes to that result and calls [onSearchResultShown]. [highlightedResult] is marked in the text.
 */
interface EpubViewHost {
    val theme: ReaderTheme
    val fontSize: Int
    val font: ReaderFont
    val jumpToProgress: Double?
    val searchedQuery: String?
    val showSearchResult: SearchResult?
    val highlightedResult: SearchResult?

    fun onJumped()
    fun onReachedEnd()
    fun onPagesLoaded(pages: List<BookPage>)
    fun onPositionChanged(locator: String, progress: Double, page: Int?)
    fun onCenterTap()
    fun onOpenFailed(message: String)
    fun onSearchResults(results: List<SearchResult>, isDone: Boolean)
    fun onSearchResultShown()
}

/**
 * One page of a book: a fixed part of about 1,000 characters (Readium's "positions"), so the page
 * count doesn't depend on screen or font size. [progress] is where it starts (0..1), [chapter] the
 * title of the chapter it's in, if known.
 */
data class BookPage(val progress: Double, val chapter: String?)

/**
 * A place in the book where the search text was found: [match] as it's written there, with the text
 * [before] and [after] it. [locator] is the exact place (only the renderer understands it), [progress]
 * where it is in the whole book (0..1), [chapter] the title of the chapter it's in, if known.
 */
data class SearchResult(
    val locator: String,
    val progress: Double,
    val chapter: String?,
    val before: String,
    val match: String,
    val after: String,
)
