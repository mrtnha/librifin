package io.github.mrtnha.librifin

import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.platform.Platform
import io.github.mrtnha.librifin.storage.BookStore
import io.github.mrtnha.librifin.sync.ProgressSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.SerializationException

/**
 * App-wide objects, shared by all screens (no DI framework). Create exactly one per app process
 * (Android: LibrifinApplication, iOS: MainViewController), not per activity or window: a second
 * [BookStore] would no longer keep the saves of the first in order. Lives as long as the process,
 * so nothing here is ever closed or cancelled.
 */
class AppServices(val platform: Platform) {
    val jellyfin = JellyfinClient(platform)
    val bookStore = BookStore(platform.filesDir)

    /**
     * For work that must finish even if the screen that started it is closed, e.g. saving the reading
     * position. Never cancelled, so it also outlives the activity when the app is left right away.
     */
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val progressSync = ProgressSync(jellyfin, bookStore, scope)

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
}
