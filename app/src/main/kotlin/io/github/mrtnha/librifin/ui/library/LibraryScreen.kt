package io.github.mrtnha.librifin.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import coil3.compose.AsyncImage
import io.github.mrtnha.librifin.AppServices
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.storage.Book
import io.github.mrtnha.librifin.ui.components.LibrifinIcons
import io.github.mrtnha.librifin.ui.components.SearchBar
import io.github.mrtnha.librifin.ui.reader.PrepareReader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    session: Session,
    services: AppServices,
    onSessionExpired: () -> Unit,
    onBookClick: (Book) -> Unit,
    onSettingsClick: () -> Unit,
) {
    val vm = viewModel {
        LibraryViewModel(
            session,
            services.jellyfin,
            services.bookStore,
            services.progressSync,
            services.platform.settingsStore,
        )
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isPullRefreshing by remember { mutableStateOf(false) }

    // Back in the app (or back from a book): refresh quietly, e.g. to leave offline mode once the
    // server is reachable again.
    LifecycleEventEffect(Lifecycle.Event.ON_START) { vm.refresh() }

    // Books are opened from here: get the reader ready, so the first one opens faster.
    PrepareReader()

    /** Loads again; says so if the server can't be reached, as nothing else would change. */
    suspend fun retry() {
        vm.load().join()
        if ((vm.state as? LibraryState.Loaded)?.isOffline == true) {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar("Unable to reach your Jellyfin server.")
        }
    }

    // Offline, a book that isn't downloaded can't be opened: say why, and offer to reconnect.
    fun explainNotDownloaded() {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = "This book isn't downloaded. Connect to your Jellyfin server to read it.",
                actionLabel = "Try again",
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) retry()
        }
    }

    val searchQuery = vm.searchQuery

    // While searching, back closes the search instead of leaving the library.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = searchQuery != null,
        onBackCompleted = { vm.searchQuery = null },
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (searchQuery != null) {
                SearchBar(
                    query = searchQuery,
                    placeholder = "Title or author",
                    onQueryChange = { vm.searchQuery = it },
                    onClose = { vm.searchQuery = null },
                )
            } else {
                TopAppBar(
                    title = { Text("Library", fontWeight = FontWeight.SemiBold) },
                    actions = {
                        IconButton(onClick = { vm.isSortSheetOpen = true }) {
                            Icon(LibrifinIcons.Sort, contentDescription = "Sort books")
                        }
                        // Second from the right, as in the reader's top bar.
                        IconButton(onClick = { vm.searchQuery = "" }) {
                            Icon(LibrifinIcons.Search, contentDescription = "Search books")
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(LibrifinIcons.Settings, contentDescription = "Settings")
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
            when (val state = vm.state) {
                LibraryState.Loading -> CircularProgressIndicator()
                LibraryState.NoBookLibrary -> Message("No book library found on this server.")
                is LibraryState.Error ->
                    if (state.isSessionExpired) {
                        Message(state.message, actionLabel = "Log in again", onAction = onSessionExpired)
                    } else {
                        Message(state.message, actionLabel = "Try again", onAction = vm::load)
                    }
                is LibraryState.Loaded -> {
                    // Nothing typed yet: no books, as showing all would look like all of them matched.
                    val books = when {
                        searchQuery == null -> state.books
                        searchQuery.isBlank() -> emptyList()
                        else -> state.books.filter { it.matches(searchQuery) }
                    }
                    when {
                        state.books.isEmpty() -> Message("No books yet.")
                        searchQuery?.isBlank() == true -> Unit
                        books.isEmpty() -> Message("No books match \u201C${searchQuery.orEmpty().trim()}\u201D.")
                        else -> PullToRefreshBox(
                            isRefreshing = isPullRefreshing,
                            onRefresh = {
                                scope.launch {
                                    isPullRefreshing = true
                                    try {
                                        retry()
                                    } finally {
                                        isPullRefreshing = false
                                    }
                                }
                            },
                        ) {
                            // A new grid for a new sort, so it starts at the top with the first book of the
                            // new order, instead of staying on the book in view, which is now elsewhere.
                            key(vm.sort) {
                                BookGrid(
                                    books = books,
                                    progress = state.progress,
                                    canOpen = state::canOpen,
                                    onBookClick = { book ->
                                        if (state.canOpen(book)) onBookClick(book) else explainNotDownloaded()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (vm.isSortSheetOpen) {
        SortSheet(selected = vm.sort, onSelect = vm::selectSort, onDismiss = { vm.isSortSheetOpen = false })
    }
}

@Composable
private fun BookGrid(
    books: List<Book>,
    progress: Map<String, BookProgress>,
    canOpen: (Book) -> Boolean,
    onBookClick: (Book) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(books, key = { it.id }) { book ->
            BookItem(book, progress[book.id], isDimmed = !canOpen(book), onClick = { onBookClick(book) })
        }
    }
}

@Composable
private fun BookItem(book: Book, progress: BookProgress?, isDimmed: Boolean, onClick: () -> Unit) {
    // Books that can't be opened right now (offline, not downloaded) are shown faded and in grey.
    Column(Modifier.clickable(onClick = onClick).alpha(if (isDimmed) DIMMED_ALPHA else 1f)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            // Placeholder, visible while the cover loads or if there is none.
            Icon(
                LibrifinIcons.Book,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp),
            )
            if (book.coverUrl != null) {
                AsyncImage(
                    model = book.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = if (isDimmed) grayscale else null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (progress?.isFinished == true) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        LibrifinIcons.Check,
                        contentDescription = "Finished",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            // Along the bottom edge of the cover, like in Jellyfin. On every book: an empty track until
            // started, then filling up in blue. The dark, see-through track stays visible on any cover.
            LinearProgressIndicator(
                progress = { progress?.shown ?: 0f },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(6.dp),
                trackColor = Color.Black.copy(alpha = 0.5f),
                // One continuous bar with square ends, flush with the cover: no gap, no dot at the end.
                strokeCap = StrokeCap.Butt,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
        }
        Text(
            book.title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun Message(text: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, modifier = Modifier.padding(top = 8.dp)) { Text(actionLabel) }
        }
    }
}

private const val DIMMED_ALPHA = 0.45f
private val grayscale = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
