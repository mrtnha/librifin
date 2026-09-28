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
import io.github.mrtnha.librifin.storage.BookStore
import io.github.mrtnha.librifin.sync.ProgressSync
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable

/**
 * What the grid shows for one book. Also saved on the device, see [io.github.mrtnha.librifin.storage.BookStore].
 * [serverProgress] (0..1) and [isPlayed] are Jellyfin's view of how far it was read.
 * [authors] is empty in lists saved before authors were loaded.
 */
@Serializable
data class Book(
    val id: String,
    val title: String,
    val coverUrl: String?,
    val serverProgress: Double? = null,
    val isPlayed: Boolean = false,
    val authors: List<String> = emptyList(),
) {
    /** Every word of [query] is in the title or an author's name, ignoring case: "tolkien hobbit" finds the book. */
    fun matches(query: String): Boolean =
        query.trim().split(Regex("\\s+")).all { word ->
            title.contains(word, ignoreCase = true) || authors.any { it.contains(word, ignoreCase = true) }
        }
}

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

    var isProfileSheetOpen by mutableStateOf(false)

    /**
     * The search text while searching, else null. Filters the books on the device, so it works offline too.
     * Kept while a book is open, so the results are still there when coming back.
     */
    var searchQuery by mutableStateOf<String?>(null)

    var isLoggingOut by mutableStateOf(false)
        private set

    private var loadJob: Job? = null

    init {
        load()
    }

    /**
     * Tells the server to end the session, then calls [onLoggedOut]. If the server can't be
     * reached, the user is still logged out locally: the token is simply forgotten.
     */
    fun logout(onLoggedOut: () -> Unit) {
        if (isLoggingOut) return
        isLoggingOut = true
        viewModelScope.launch {
            try {
                withTimeoutOrNull(LOGOUT_TIMEOUT_MS) { jellyfin.logout(session) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Server unreachable or token already invalid: nothing left to end on the server.
            }
            onLoggedOut()
        }
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
            if (state !is LibraryState.Loaded) {
                state = withContext(Dispatchers.IO) { bookStore.readLibrary(session) }
                    ?.let { LibraryState.Loaded(it, progressOf(it)) }
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
                        )
                    }
                    withContext(Dispatchers.IO) { bookStore.saveLibrary(session, books) }
                    // The server is reachable again: send what was read offline.
                    progressSync.sendUnsynced(session)
                    LibraryState.Loaded(books, progressOf(books))
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
                        LibraryState.Loaded(saved, progressOf(saved), isOffline = true, downloadedIds = downloaded)
                    }
                    else -> LibraryState.Error(e.toUserMessage())
                }
            }
        }.also { loadJob = it }
    }

    /**
     * This device's progress where there is one (exact, and newest for books read here), else
     * Jellyfin's (e.g. read on another device). The same for whether it's finished.
     */
    private suspend fun progressOf(books: List<Book>): Map<String, BookProgress> {
        val positions = withContext(Dispatchers.IO) { bookStore.readAllPositions(session) }
        return books.mapNotNull { book ->
            val local = positions[book.id]
            val progress = BookProgress(
                fraction = (local?.progress ?: book.serverProgress ?: 0.0).toFloat(),
                isFinished = local?.isFinished ?: book.isPlayed,
            )
            (book.id to progress).takeIf { progress.isFinished || progress.fraction > 0f }
        }.toMap()
    }

    private companion object {
        /** Half the width of a large phone screen in pixels, so covers stay sharp in a two-column grid. */
        const val COVER_MAX_WIDTH = 600
        const val LOGOUT_TIMEOUT_MS = 5_000L
    }
}
