package io.github.mrtnha.librifin.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import io.github.mrtnha.librifin.AppContainer
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.ui.components.LibrifinIcons
import io.github.mrtnha.librifin.ui.components.SearchBar
import io.github.mrtnha.librifin.ui.theme.readerBarsColorScheme
import io.github.mrtnha.librifin.ui.theme.readerColorScheme
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/** Shows one book full screen. The app bar lies on top of the book, so the book doesn't move when it appears. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    session: Session,
    bookId: String,
    title: String,
    appContainer: AppContainer,
    onBack: () -> Unit,
) {
    val vm = viewModel {
        ReaderViewModel(
            session,
            bookId,
            appContainer.jellyfin,
            appContainer.bookStore,
            appContainer.progressSync,
            appContainer.settingsStore,
            appContainer.scope,
        )
    }

    // Back in the app: the book may have been read further elsewhere meanwhile.
    LifecycleEventEffect(Lifecycle.Event.ON_START) { vm.onAppResumed() }

    // Full screen only while the book is shown; while loading or on errors the bars stay, so the way back is visible.
    // So does the search bar while a result is marked: it's the way out of the search.
    val isResultMarked = vm.highlightedResult != null
    val showBars = vm.state !is ReaderState.Ready || vm.areBarsVisible || vm.isSearchOpen || isResultMarked
    val theme = vm.theme
    SystemBarsVisible(showBars, darkBackground = theme.bars.luminance() < 0.5f)
    val barsColors = readerBarsColorScheme(theme.bars)

    // While searching or a result is marked, back ends the search instead of leaving the book.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = vm.isSearchOpen || isResultMarked,
        onBackCompleted = vm::closeSearch,
    )
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = vm.isAppearanceOpen,
        onBackCompleted = vm::closeAppearance,
    )

    // The whole screen in the page's colors, also while loading; the bars stand apart in their own.
    MaterialTheme(colorScheme = readerColorScheme(theme.isDark, theme.background, theme.text)) {
        Surface(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {
                when (val state = vm.state) {
                    is ReaderState.Ready ->
                        EpubView(state.file, state.startLocator, host = vm, modifier = Modifier.fillMaxSize())
                    else -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        when (state) {
                            is ReaderState.Downloading ->
                                if (state.progress == null) {
                                    CircularProgressIndicator()
                                } else {
                                    CircularProgressIndicator(progress = { state.progress })
                                }
                            is ReaderState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message, textAlign = TextAlign.Center)
                                if (state.canRetry) {
                                    TextButton(onClick = vm::load, modifier = Modifier.padding(top = 8.dp)) { Text("Try again") }
                                }
                            }
                            is ReaderState.Ready -> Unit
                        }
                    }
                }

                // Along the bottom, with the app bar: drag to move through the whole book.
                AnimatedVisibility(
                    visible = showBars && vm.pages.size > 1,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    MaterialTheme(colorScheme = barsColors) {
                        PageSlider(pages = vm.pages, currentPage = vm.currentPage, onPageSelected = vm::jumpToPage)
                    }
                }

                AnimatedVisibility(visible = showBars, enter = fadeIn(), exit = fadeOut()) {
                    MaterialTheme(colorScheme = barsColors) {
                        if (isResultMarked) {
                            SearchResultBar(vm)
                        } else {
                            TopAppBar(
                                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                // Reserve room for the status bar even while it's hidden, so the app bar doesn't jump
                                // down once the status bar has finished appearing.
                                windowInsets = systemBarsIgnoringVisibility()
                                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                                navigationIcon = {
                                    IconButton(onClick = onBack) {
                                        Icon(LibrifinIcons.ArrowBack, contentDescription = "Back")
                                    }
                                },
                                actions = {
                                    // Also while the book is loading: the search runs as soon as it's open.
                                    IconButton(onClick = vm::openSearch, enabled = vm.state !is ReaderState.Error) {
                                        Icon(LibrifinIcons.Search, contentDescription = "Search in book")
                                    }
                                    IconButton(onClick = vm::openAppearance) {
                                        Icon(LibrifinIcons.MatchCase, contentDescription = "Appearance")
                                    }
                                },
                            )
                        }
                    }
                }

                // Over the book: a tap on the page closes the sheet instead of turning the page.
                if (vm.isAppearanceOpen) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .clickable(interactionSource = null, indication = null, onClick = vm::closeAppearance),
                    )
                }
                AnimatedVisibility(
                    visible = vm.isAppearanceOpen,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    MaterialTheme(colorScheme = barsColors) {
                        AppearanceSheet(
                            theme = theme,
                            onThemeSelected = vm::selectTheme,
                            canDecreaseFontSize = vm.canDecreaseFontSize,
                            canIncreaseFontSize = vm.canIncreaseFontSize,
                            onDecreaseFontSize = vm::decreaseFontSize,
                            onIncreaseFontSize = vm::increaseFontSize,
                            font = vm.font,
                            onFontSelected = vm::selectFont,
                            onClose = vm::closeAppearance,
                        )
                    }
                }

                // Over everything, the book included, so taps on the results don't turn pages.
                if (vm.isSearchOpen) {
                    MaterialTheme(colorScheme = barsColors) {
                        SearchPanel(vm, title)
                    }
                }
            }
        }
    }
}

/**
 * The app bar while a picked result is marked in the book: the search text, which brings back the results
 * when tapped, which match this is, and arrows to the previous and next one. The arrow ends the search.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResultBar(vm: ReaderViewModel) {
    TopAppBar(
        windowInsets = systemBarsIgnoringVisibility().only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        navigationIcon = {
            IconButton(onClick = vm::closeSearch) {
                Icon(LibrifinIcons.ArrowBack, contentDescription = "Close search")
            }
        },
        title = {
            // Where the search field shows it, so the text stays in place when the results come back.
            Text(
                vm.searchQuery,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClickLabel = "Show results", onClick = vm::openSearch)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        },
        actions = {
            // "of", not "/": the page slider below shows pages as "57/412".
            vm.highlightedIndex?.let { index ->
                Text(
                    "${index + 1} of ${vm.searchResults.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            IconButton(onClick = vm::showPreviousResult, enabled = vm.canShowPreviousResult) {
                Icon(LibrifinIcons.KeyboardArrowUp, contentDescription = "Previous match")
            }
            IconButton(onClick = vm::showNextResult, enabled = vm.canShowNextResult) {
                Icon(LibrifinIcons.KeyboardArrowDown, contentDescription = "Next match")
            }
        },
    )
}

/** The search field in the app bar and the results below it, in the colors of the bars. */
@Composable
private fun SearchPanel(vm: ReaderViewModel, title: String) {
    val keyboard = LocalSoftwareKeyboardController.current
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.windowInsetsPadding(
                WindowInsets.ime.union(WindowInsets.navigationBars).only(WindowInsetsSides.Bottom),
            ),
        ) {
            SearchBar(
                query = vm.searchQuery,
                placeholder = "Search in $title",
                onQueryChange = vm::onSearchQueryChange,
                onClose = vm::closeSearch,
                windowInsets = systemBarsIgnoringVisibility().only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
            )
            val query = vm.searchedQuery ?: return@Column
            if (vm.isSearching) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            val results = vm.searchResults
            when {
                results.isNotEmpty() -> SearchResults(
                    results = results,
                    selected = vm.highlightedResult,
                    pageCount = vm.pages.size,
                    pageOf = vm::pageOf,
                    onSelect = { result ->
                        keyboard?.hide()
                        vm.selectSearchResult(result)
                    },
                )
                !vm.isSearching -> Text(
                    "No matches for \u201C$query\u201D.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                )
            }
        }
    }
}

/**
 * One row per match: the chapter and page, then the text around the match with the match in bold.
 * Opens at the result picked last, so coming back to the list continues where it was left.
 */
@Composable
private fun SearchResults(
    results: List<SearchResult>,
    selected: SearchResult?,
    pageCount: Int,
    pageOf: (SearchResult) -> Int?,
    onSelect: (SearchResult) -> Unit,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = results.indexOf(selected).coerceAtLeast(0))
    val matchColor = MaterialTheme.colorScheme.primary
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                if (results.size == 1) "1 result" else "${results.size} results",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
        items(results) { result ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(result) }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Row {
                    Text(
                        result.chapter.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    pageOf(result)?.let { page ->
                        Text(
                            "$page/$pageCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                }
                Text(
                    snippet(result, matchColor),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

/**
 * The match in bold with the text around it, on one line. Only the last few words before it, so the
 * match is on the first line.
 */
private fun snippet(result: SearchResult, matchColor: Color): AnnotatedString {
    val before = result.before.oneLine().trimStart()
    val shortBefore = if (before.length <= SNIPPET_BEFORE_LENGTH) {
        before
    } else {
        "\u2026" + before.takeLast(SNIPPET_BEFORE_LENGTH).substringAfter(' ')
    }
    return buildAnnotatedString {
        append(shortBefore)
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = matchColor)) { append(result.match.oneLine()) }
        append(result.after.oneLine().trimEnd())
    }
}

/** Line breaks and runs of spaces from the book's layout, as single spaces. */
private fun String.oneLine() = replace(WHITESPACE, " ")

private val WHITESPACE = Regex("\\s+")

/** How much of the text before a match its result shows, at most. */
private const val SNIPPET_BEFORE_LENGTH = 40

/**
 * The page slider: the chapter and "page/pages" above a slider through the whole book. While dragging,
 * they follow the finger, and the book shows that page whenever the finger rests for a moment
 * (not on every move, which would make it flicker); letting go shows it right away.
 */
@Composable
private fun PageSlider(pages: List<BookPage>, currentPage: Int?, onPageSelected: (Int) -> Unit) {
    var draggedPage by remember { mutableStateOf<Int?>(null) }
    val page = (draggedPage ?: currentPage ?: 1).coerceIn(1, pages.size)

    LaunchedEffect(draggedPage) {
        val target = draggedPage ?: return@LaunchedEffect
        delay(PREVIEW_DELAY_MS)
        onPageSelected(target)
    }

    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier
                .fillMaxWidth()
                // Like the app bar: room for the navigation bar even while it's hidden, so nothing jumps.
                .windowInsetsPadding(
                    systemBarsIgnoringVisibility().only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                )
                // The text sits 24 dp from the edges and the slider's line ends right below it.
                .padding(start = 24.dp - THUMB_SIZE / 2, end = 24.dp - THUMB_SIZE / 2, top = 12.dp),
        ) {
            Row(Modifier.padding(horizontal = THUMB_SIZE / 2), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    pages[page - 1].chapter.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "$page/${pages.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
            Slider(
                value = page.toFloat(),
                onValueChange = { draggedPage = it.roundToInt() },
                onValueChangeFinished = {
                    draggedPage?.let(onPageSelected)
                    draggedPage = null
                },
                valueRange = 1f..pages.size.toFloat(),
                // A bit closer to the text, with that room added below; the touch area keeps its full height.
                modifier = Modifier.offset(y = (-4).dp),
                // A thin line with a round dot instead of Material's thick track, tall handle, gaps and end dot.
                thumb = { Box(Modifier.size(THUMB_SIZE).background(MaterialTheme.colorScheme.primary, CircleShape)) },
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        modifier = Modifier.height(6.dp),
                        colors = SliderDefaults.colors(inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant),
                        drawStopIndicator = null,
                        thumbTrackGapSize = 0.dp,
                    )
                },
            )
        }
    }
}

/** The page slider's dot. Material shortens the slider's line by half of it on each side. */
private val THUMB_SIZE = 16.dp

/** How long the finger has to rest on the page slider before the book shows that page. */
private const val PREVIEW_DELAY_MS = 50L
