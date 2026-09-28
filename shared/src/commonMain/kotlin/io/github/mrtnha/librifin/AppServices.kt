package io.github.mrtnha.librifin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.platform.Platform
import io.github.mrtnha.librifin.storage.BookStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.SerializationException

/**
 * App-wide objects, created once and shared by all screens (no DI framework).
 * A ViewModel so it survives configuration changes and is closed with the app.
 */
class AppServices(val platform: Platform) : ViewModel() {
    val jellyfin = JellyfinClient(platform)
    val bookStore = BookStore(platform.filesDir)

    /** For work that must finish even if the screen that started it is closed, e.g. saving the reading position. */
    val scope: CoroutineScope get() = viewModelScope

    /** The session saved at the last login, or null if logged out (or the saved data is unreadable). */
    fun loadSession(): Session? {
        val saved = platform.sessionStore.read() ?: return null
        return try {
            JellyfinClient.json.decodeFromString<Session>(saved)
        } catch (_: SerializationException) {
            platform.sessionStore.clear()
            null
        } catch (_: IllegalArgumentException) {
            platform.sessionStore.clear()
            null
        }
    }

    fun saveSession(session: Session) = platform.sessionStore.write(JellyfinClient.json.encodeToString(session))

    fun clearSession() = platform.sessionStore.clear()

    override fun onCleared() = jellyfin.close()
}
