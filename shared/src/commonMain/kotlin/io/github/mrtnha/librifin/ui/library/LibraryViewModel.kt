package io.github.mrtnha.librifin.ui.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.api.toUserMessage
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** What the grid shows for one book. */
data class Book(val id: String, val title: String, val coverUrl: String?)

sealed interface LibraryState {
    data object Loading : LibraryState
    data object NoBookLibrary : LibraryState
    data class Loaded(val books: List<Book>) : LibraryState
    /** [isSessionExpired]: the server no longer accepts the token, so retrying won't help. */
    data class Error(val message: String, val isSessionExpired: Boolean = false) : LibraryState
}

class LibraryViewModel(
    private val session: Session,
    private val jellyfin: JellyfinClient,
) : ViewModel() {
    var state by mutableStateOf<LibraryState>(LibraryState.Loading)
        private set

    var isProfileSheetOpen by mutableStateOf(false)
    var isLoggingOut by mutableStateOf(false)
        private set

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

    fun load() {
        state = LibraryState.Loading
        viewModelScope.launch {
            state = try {
                val libraries = jellyfin.userViews(session).filter { it.collectionType == "books" }
                if (libraries.isEmpty()) {
                    LibraryState.NoBookLibrary
                } else {
                    // Usually one book library; if there are several, show all their books.
                    val books = libraries.flatMap { jellyfin.books(session, it.id) }
                    LibraryState.Loaded(
                        books.map { item ->
                            Book(
                                id = item.id,
                                title = item.name ?: "Untitled",
                                coverUrl = jellyfin.primaryImageUrl(session.server.baseUrl, item, COVER_MAX_WIDTH),
                            )
                        },
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ClientRequestException) {
                if (e.response.status.value == 401) {
                    LibraryState.Error("Your session has expired. Please log in again.", isSessionExpired = true)
                } else {
                    LibraryState.Error(e.toUserMessage())
                }
            } catch (e: Exception) {
                LibraryState.Error(e.toUserMessage())
            }
        }
    }

    private companion object {
        /** Half the width of a large phone screen in pixels, so covers stay sharp in a two-column grid. */
        const val COVER_MAX_WIDTH = 600
        const val LOGOUT_TIMEOUT_MS = 5_000L
    }
}
