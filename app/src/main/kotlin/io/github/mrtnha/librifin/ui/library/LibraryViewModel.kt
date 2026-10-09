package io.github.mrtnha.librifin.ui.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mrtnha.librifin.api.BOOK_PROGRESS_TICKS
import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.api.clientErrorStatus
import io.github.mrtnha.librifin.api.toUserMessage
import io.github.mrtnha.librifin.storage.Book
import io.github.mrtnha.librifin.storage.BookStore
import io.github.mrtnha.librifin.sync.ProgressSync
import io.ktor.http.HttpStatusCode
import kotlin.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** How far a book was read: [fraction] 0..1, [isFinished] once the book was read to the end (or nearly). */
data class BookProgress(val fraction: Float, val isFinished: Boolean)

sealed interface LibraryState {
    data object Loading : LibraryState
    data object NoBookLibrary : LibraryState
    /**
     * [progress]: only for books that were started. [isOffline]: the server can't be reached, so
     * this is the saved list and only the books in [downloadedIds] can be opened.
     */
    data class Loaded(
        val books: List<Book>,
        val progress: Map<String, BookProgress>,
        val isOffline: Boolean = false,
        val downloadedIds: Set<String> = emptySet(),
    ) : LibraryState {
        fun canOpen(book: Book) = !isOffline || book.id in downloadedIds
    }
    /** [isSessionExpired]: the server no longer accepts the token, so retrying won't help. */
    data class Error(val message: String, val isSessionExpired: Boolean = false) : LibraryState
}

class LibraryViewModel(
    private val session: Session,
    private val jellyfin: JellyfinClient,
    private val bookStore: BookStore,
    private val progressSync: ProgressSync,
) : ViewModel() {
    var state by mutableStateOf<LibraryState>(LibraryState.Loading)
        private set

    /**
     * The search text while searching, else null. Filters the books on the device, so it works offline too.
     * Kept while a book is open, so the results are still there when coming back.
     */
    var searchQuery by mutableStateOf<String?>(null)

    private var loadJob: Job? = null

    init {
        load()
    }

    /** Loads again, unless a load is already running. */
    fun refresh() {
        if (loadJob?.isActive != true) load()
    }

    /**
     * Loads the books from the server. The saved list is shown meanwhile, and stays when the server
     * can't be reached: then only the downloaded books can be opened.
     */
    fun load(): Job {
        loadJob?.cancel()
        return viewModelScope.launch {
            val shown = state as? LibraryState.Loaded
            state = if (shown != null) {
                // E.g. back from a book: its progress and place in the order are on this device already,
                // so they show right away, not only once the server has answered below.
                loaded(shown.books, shown.isOffline, shown.downloadedIds)
            } else {
                withContext(Dispatchers.IO) { bookStore.readLibrary(session) }?.let { loaded(it) }
                    ?: LibraryState.Loading
            }
            val saved = (state as? LibraryState.Loaded)?.books
            state = try {
                val libraries = jellyfin.userViews(session).filter { it.collectionType == "books" }
                if (libraries.isEmpty()) {
                    LibraryState.NoBookLibrary
                } else {
                    // Usually one book library; if there are several, show all their books.
                    val books = libraries.flatMap { jellyfin.books(session, it.id) }.map { item ->
                        Book(
                            id = item.id,
                            title = item.name ?: "Untitled",
                            coverUrl = jellyfin.primaryImageUrl(session.server.baseUrl, item, COVER_MAX_WIDTH),
                            serverProgress = item.userData?.playbackPositionTicks
                                ?.takeIf { it > 0 }
                                ?.let { (it.toDouble() / BOOK_PROGRESS_TICKS).coerceIn(0.0, 1.0) },
                            isPlayed = item.userData?.played == true,
                            authors = item.people.orEmpty().filter { it.type == "Author" }.mapNotNull { it.name },
                            lastPlayedMillis = item.userData?.lastPlayedDate
                                ?.let { Instant.parseOrNull(it) }
                                ?.toEpochMilliseconds(),
                        )
                    }
                    withContext(Dispatchers.IO) { bookStore.saveLibrary(session, books) }
                    // The server is reachable again: send what was read offline.
                    progressSync.sendUnsynced(session)
                    loaded(books)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                when {
                    e.clientErrorStatus == HttpStatusCode.Unauthorized ->
                        LibraryState.Error(e.toUserMessage(), isSessionExpired = true)
                    saved != null -> {
                        val downloaded = withContext(Dispatchers.IO) {
                            saved.filter { bookStore.isDownloaded(it.id) }.mapTo(HashSet()) { it.id }
                        }
                        loaded(saved, isOffline = true, downloadedIds = downloaded)
                    }
                    else -> LibraryState.Error(e.toUserMessage())
                }
            }
        }.also { loadJob = it }
    }

    /**
     * [books] as the grid shows them, with this device's progress where there is one (exact, and newest
     * for books read here), else Jellyfin's (e.g. read on another device); the same for whether it's
     * finished.
     *
     * Most recently read first: the later of when a book was read here and when Jellyfin last saw it
     * read, so a book read here moves up right away, also offline, and one read on another device still
     * does. Only turning a page counts as reading, not just opening a book. Books never read keep their
     * order, by title, after them.
     */
    private suspend fun loaded(
        books: List<Book>,
        isOffline: Boolean = false,
        downloadedIds: Set<String> = emptySet(),
    ): LibraryState.Loaded {
        // After the saves still running: leaving a book right after a page turn may not have finished saving it.
        val positions = bookStore.currentPositions(session)
        val progress = books.mapNotNull { book ->
            val local = positions[book.id]
            val progress = BookProgress(
                fraction = (local?.progress ?: book.serverProgress ?: 0.0).toFloat(),
                isFinished = local?.isFinished ?: book.isPlayed,
            )
            (book.id to progress).takeIf { progress.isFinished || progress.fraction > 0f }
        }.toMap()
        val ordered = books.sortedByDescending { book ->
            maxOf(positions[book.id]?.updatedAtMillis ?: NEVER_READ, book.lastPlayedMillis ?: NEVER_READ)
        }
        return LibraryState.Loaded(ordered, progress, isOffline, downloadedIds)
    }

    private companion object {
        /** Half the width of a large phone screen in pixels, so covers stay sharp in a two-column grid. */
        const val COVER_MAX_WIDTH = 600

        /** Sorts after every book that was read. */
        const val NEVER_READ = Long.MIN_VALUE
    }
}
