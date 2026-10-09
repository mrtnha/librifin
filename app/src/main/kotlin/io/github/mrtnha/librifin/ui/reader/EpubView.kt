@file:OptIn(ExperimentalReadiumApi::class)

package io.github.mrtnha.librifin.ui.reader

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentFactory
import androidx.fragment.compose.AndroidFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mrtnha.librifin.ui.theme.JellyfinBlue
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import org.json.JSONException
import org.json.JSONObject
import org.readium.r2.navigator.Decoration
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.epub.css.FontStyle
import org.readium.r2.navigator.input.InputListener
import org.readium.r2.navigator.input.TapEvent
import org.readium.r2.navigator.preferences.Color
import org.readium.r2.navigator.preferences.FontFamily
import org.readium.r2.navigator.preferences.Theme
import org.readium.r2.navigator.util.DirectionalNavigationAdapter
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.firstWithHref
import org.readium.r2.shared.publication.flatten
import org.readium.r2.shared.publication.services.isRestricted
import org.readium.r2.shared.publication.services.locateProgression
import org.readium.r2.shared.publication.services.positions
import org.readium.r2.shared.publication.services.search.search
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.getOrElse
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser

/**
 * Renders the EPUB in [file] paginated, starting at [initialLocator] (a value from
 * [EpubViewHost.onPositionChanged]), else at the beginning. [host] says how, and hears what happens.
 */
@Composable
fun EpubView(file: Path, initialLocator: String?, host: EpubViewHost, modifier: Modifier = Modifier) {
    val application = LocalContext.current.applicationContext as Application
    val vm = viewModel { EpubViewModel(application, File(file.toString()), initialLocator, host) }

    when (val state = vm.state) {
        EpubState.Opening -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is EpubState.Failed -> LaunchedEffect(state) { host.onOpenFailed(state.message) }
        EpubState.Opened -> {
            LaunchedEffect(vm) { host.onPagesLoaded(vm.pages) }
            var navigatorNow by remember { mutableStateOf<EpubNavigatorFragment?>(null) }

            // The first theme, size and font are set when the book opens; later ones change the page in place.
            LaunchedEffect(host.theme, host.fontSize, host.font, navigatorNow) {
                navigatorNow?.submitPreferences(host.epubPreferences())
            }

            LaunchedEffect(host.jumpToProgress, navigatorNow) {
                val progress = host.jumpToProgress ?: return@LaunchedEffect
                val navigator = navigatorNow ?: return@LaunchedEffect
                vm.locate(progress)?.let { navigator.go(it) }
                host.onJumped()
            }

            // The search runs in the view model, so it goes on while the screen rotates.
            LaunchedEffect(host.searchedQuery) { vm.search(host.searchedQuery) }
            val search = vm.search
            LaunchedEffect(search) {
                if (search != null && search.query == host.searchedQuery) host.onSearchResults(search.results, search.isDone)
            }

            LaunchedEffect(host.showSearchResult, navigatorNow) {
                val result = host.showSearchResult ?: return@LaunchedEffect
                val navigator = navigatorNow ?: return@LaunchedEffect
                parseLocator(result.locator)?.let { navigator.go(it) }
                host.onSearchResultShown()
            }

            LaunchedEffect(host.highlightedResult, navigatorNow) {
                val navigator = navigatorNow ?: return@LaunchedEffect
                val decoration = host.highlightedResult?.let { result ->
                    parseLocator(result.locator)?.let {
                        Decoration(id = SEARCH_DECORATIONS, locator = it, style = Decoration.Style.Highlight(JellyfinBlue.toArgb()))
                    }
                }
                navigator.applyDecorations(listOfNotNull(decoration), SEARCH_DECORATIONS)
            }

            // Created by ReaderFragmentFactory, from the book the view model opened.
            // Called once per fragment instance (also the new one after rotation), so listeners are added once.
            AndroidFragment<EpubNavigatorFragment>(modifier) { navigator ->
                navigatorNow = navigator
                // Taps near the left and right edges turn the page with a slide, like a swipe.
                // Taps elsewhere reach the next listener.
                navigator.addInputListener(DirectionalNavigationAdapter(navigator, animatedTransition = true))
                navigator.addInputListener(object : InputListener {
                    override fun onTap(event: TapEvent): Boolean {
                        host.onCenterTap()
                        return true
                    }
                })
                navigator.lifecycleScope.launch {
                    navigator.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        navigator.currentLocator.collect { locator ->
                            host.onPositionChanged(
                                locator.toJSON().toString(),
                                locator.locations.totalProgression ?: 0.0,
                                locator.locations.position,
                            )
                        }
                    }
                }
            }
        }
    }
}

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

/** The group of Readium decorations that marks the picked search result. */
private const val SEARCH_DECORATIONS = "search"

/** A position from [Locator.toJSON], or null if it isn't one. */
private fun parseLocator(json: String): Locator? =
    try {
        Locator.fromJSON(JSONObject(json))
    } catch (_: JSONException) {
        null
    }

/**
 * All of the reader's settings at once, so changing one never resets another.
 * Readium's night mode for the dark themes, with our colors on top: it also recolors headings and
 * links, which keep the book's own (often black) colors otherwise.
 */
private fun EpubViewHost.epubPreferences() = EpubPreferences(
    theme = if (theme.isDark) Theme.DARK else Theme.LIGHT,
    backgroundColor = Color(theme.background.toArgb()),
    textColor = Color(theme.text.toArgb()),
    fontSize = fontSize / 100.0,
    // No font family: the book's own.
    fontFamily = font.takeIf { it != ReaderFont.ORIGINAL }?.let { FontFamily(it.label) },
)

/** Lets the book's pages use the fonts bundled with the app, under their [ReaderFont.label]s. */
private fun fontConfiguration() = EpubNavigatorFragment.Configuration {
    servedAssets += FONT_ASSETS
    ReaderFont.entries.filter { it.faces.isNotEmpty() }.forEach { font ->
        addFontFamilyDeclaration(FontFamily(font.label)) {
            font.faces.forEach { face ->
                addFontFace {
                    addSource(face.assetPath())
                    setFontStyle(if (face.isItalic) FontStyle.ITALIC else FontStyle.NORMAL)
                    setFontWeight(face.weights)
                }
            }
        }
    }
}

/** A search through the book: the [results] for [query] found so far, all of them once [isDone]. */
private data class BookSearch(val query: String, val results: List<SearchResult>, val isDone: Boolean)

private sealed interface EpubState {
    data object Opening : EpubState
    data object Opened : EpubState
    class Failed(val message: String) : EpubState
}

/**
 * Opens the book with Readium and keeps it open while the reader is on the back stack
 * (so it survives rotation). The publication is closed when the reader is left.
 * [host] is the reader's view model, which lives on that back stack entry as long as this one.
 */
private class EpubViewModel(
    private val application: Application,
    private val file: File,
    private val initialLocator: String?,
    private val host: EpubViewHost,
) : ViewModel() {
    var state by mutableStateOf<EpubState>(EpubState.Opening)
        private set

    /** The book's pages, known once it's open. */
    var pages: List<BookPage> = emptyList()
        private set

    /** The current search, null if there is none. */
    var search by mutableStateOf<BookSearch?>(null)
        private set

    private var publication: Publication? = null
    private var fragmentFactory: FragmentFactory? = null
    private var searchJob: Job? = null

    init {
        viewModelScope.launch { state = open() }
    }

    private suspend fun open(): EpubState {
        val httpClient = DefaultHttpClient()
        val assetRetriever = AssetRetriever(application.contentResolver, httpClient)
        val opener = PublicationOpener(
            DefaultPublicationParser(application, httpClient, assetRetriever, pdfFactory = null),
        )

        val asset = assetRetriever.retrieve(file).getOrElse { return EpubState.Failed(NOT_AN_EPUB) }
        val publication = opener.open(asset, allowUserInteraction = false).getOrElse {
            asset.close()
            return EpubState.Failed(NOT_AN_EPUB)
        }
        this.publication = publication

        when {
            !publication.conformsTo(Publication.Profile.EPUB) -> return EpubState.Failed(NOT_AN_EPUB)
            publication.isRestricted -> return EpubState.Failed("This book is protected (DRM) and can't be opened.")
        }

        val startLocator = initialLocator?.let(::parseLocator)?.let { saved ->
            // A position naming a file the book doesn't have (e.g. from another edition) would leave Readium
            // waiting for that file forever: no page is reported and the slider stops working. Open at the
            // same point of the whole book instead.
            if (publication.readingOrder.firstWithHref(saved.href) != null) {
                saved
            } else {
                saved.locations.totalProgression?.let { publication.locateProgression(it) }
            }
        }
        pages = pagesOf(publication)
        val lastChapter = publication.readingOrder.lastOrNull()?.url()
        val factory = EpubNavigatorFactory(publication).createFragmentFactory(
            initialLocator = startLocator,
            initialPreferences = host.epubPreferences(),
            // Tells the page within the chapter: the most reliable way to see the book's last page.
            paginationListener = object : EpubNavigatorFragment.PaginationListener {
                override fun onPageChanged(pageIndex: Int, totalPages: Int, locator: Locator) {
                    if (locator.href == lastChapter && pageIndex == totalPages - 1) host.onReachedEnd()
                }
            },
            configuration = fontConfiguration(),
        )
        fragmentFactory = factory
        ReaderFragmentFactory.epub = factory
        return EpubState.Opened
    }

    /**
     * Readium's positions, each with the title of its chapter: the table of contents entry of its
     * file, or of an earlier file if this one has none (e.g. a chapter split into several files).
     */
    private suspend fun pagesOf(publication: Publication): List<BookPage> {
        val titleByFile = publication.tableOfContents.flatten()
            .filter { it.title != null }
            .reversed() // So the first entry of a file wins in associate below.
            .associate { it.url().removeFragment() to it.title }
        var chapter: String? = null
        return publication.positions().map { position ->
            chapter = titleByFile[position.href.removeFragment()] ?: chapter
            BookPage(progress = position.locations.totalProgression ?: 0.0, chapter = chapter)
        }
    }

    /**
     * Searches the book for [query] (null ends the search), with Readium's search: case and accents
     * don't matter. The results come chapter by chapter, so the first ones show right away.
     * A new query stops the search for the old one.
     */
    fun search(query: String?) {
        if (query == search?.query) return
        searchJob?.cancel()
        if (query == null) {
            search = null
            return
        }
        val publication = publication ?: return
        search = BookSearch(query, emptyList(), isDone = false)
        searchJob = viewModelScope.launch {
            val results = mutableListOf<SearchResult>()
            val iterator = publication.search(query)
            try {
                // Each step reads and searches one chapter; a failing chapter ends the search with what was found.
                while (iterator != null) {
                    val page = withContext(Dispatchers.IO) { iterator.next() }.getOrNull() ?: break
                    page.locators.mapTo(results) { it.toSearchResult() }
                    search = BookSearch(query, results.toList(), isDone = false)
                }
            } finally {
                iterator?.close()
            }
            search = BookSearch(query, results.toList(), isDone = true)
        }
    }

    private fun Locator.toSearchResult() = SearchResult(
        locator = toJSON().toString(),
        progress = locations.totalProgression ?: 0.0,
        chapter = title,
        before = text.before.orEmpty(),
        match = text.highlight.orEmpty(),
        after = text.after.orEmpty(),
    )

    /** The place [progress] (0..1) through the whole book. */
    suspend fun locate(progress: Double): Locator? = publication?.locateProgression(progress)

    override fun onCleared() {
        if (ReaderFragmentFactory.epub === fragmentFactory) ReaderFragmentFactory.epub = null
        publication?.close()
    }

    private companion object {
        const val NOT_AN_EPUB = "This book can't be opened. Only EPUB files are supported."
    }
}
