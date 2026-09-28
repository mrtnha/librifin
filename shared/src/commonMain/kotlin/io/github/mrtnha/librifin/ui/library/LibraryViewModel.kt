package io.github.mrtnha.librifin.ui.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.api.toUserMessage
import io.github.mrtnha.librifin.storage.BookStore
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable

/** What the grid shows for one book. Also saved on the device, see [io.github.mrtnha.librifin.storage.BookStore]. */
@Serializable
data class Book(val id: String, val title: String, val coverUrl: String?)

sealed interface LibraryState {
    data object Loading : LibraryState
    data object NoBookLibrary : LibraryState
    /**
     * [isOffline]: the server can't be reached, so this is the saved list and only the books in
     * [downloadedIds] can be opened.
     */
    data class Loaded(
        val books: List<Book>,
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
) : ViewModel() {
    var state by mutableStateOf<LibraryState>(LibraryState.Loading)
        private set

    var isProfileSheetOpen by mutableStateOf(false)
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

    /**
     * Loads the books from the server. The saved list is shown meanwhile, and stays when the server
     * can't be reached: then only the downloaded books can be opened.
     */
    fun load(): Job {
        loadJob?.cancel()
        return viewModelScope.launch {
            if (state !is LibraryState.Loaded) {
                state = withContext(Dispatchers.IO) { bookStore.readLibrary(session) }
                    ?.let { LibraryState.Loaded(it) }
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
                        )
                    }
                    withContext(Dispatchers.IO) { bookStore.saveLibrary(session, books) }
                    LibraryState.Loaded(books)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                when {
                    (e as? ClientRequestException)?.response?.status?.value == 401 ->
                        LibraryState.Error("Your session has expired. Please log in again.", isSessionExpired = true)
                    saved != null -> {
                        val downloaded = withContext(Dispatchers.IO) {
                            saved.filter { bookStore.isDownloaded(it.id) }.mapTo(HashSet()) { it.id }
                        }
                        LibraryState.Loaded(saved, isOffline = true, downloadedIds = downloaded)
                    }
                    else -> LibraryState.Error(e.toUserMessage())
                }
            }
        }.also { loadJob = it }
    }

    private companion object {
        /** Half the width of a large phone screen in pixels, so covers stay sharp in a two-column grid. */
        const val COVER_MAX_WIDTH = 600
        const val LOGOUT_TIMEOUT_MS = 5_000L
    }
}
