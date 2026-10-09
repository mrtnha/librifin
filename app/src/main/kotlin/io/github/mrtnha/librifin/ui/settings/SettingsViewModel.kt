package io.github.mrtnha.librifin.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.storage.BookStore
import io.github.mrtnha.librifin.storage.Downloads
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class SettingsViewModel(
    private val session: Session,
    private val jellyfin: JellyfinClient,
    private val bookStore: BookStore,
) : ViewModel() {
    var isLoggingOut by mutableStateOf(false)
        private set

    /** The downloaded books, or null while they're being counted. */
    var downloads by mutableStateOf<Downloads?>(null)
        private set

    init {
        viewModelScope.launch {
            downloads = withContext(Dispatchers.IO) { bookStore.downloads() }
        }
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

    /** Deletes the downloaded books. They download again when opened; reading positions stay. */
    fun removeDownloads() {
        viewModelScope.launch {
            downloads = withContext(Dispatchers.IO) {
                bookStore.deleteAllBooks()
                bookStore.downloads()
            }
        }
    }

    private companion object {
        const val LOGOUT_TIMEOUT_MS = 5_000L
    }
}
