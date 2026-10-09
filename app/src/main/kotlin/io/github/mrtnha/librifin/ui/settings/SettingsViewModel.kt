package io.github.mrtnha.librifin.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Session
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class SettingsViewModel(
    private val session: Session,
    private val jellyfin: JellyfinClient,
) : ViewModel() {
    var isLoggingOut by mutableStateOf(false)
        private set

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

    private companion object {
        const val LOGOUT_TIMEOUT_MS = 5_000L
    }
}
