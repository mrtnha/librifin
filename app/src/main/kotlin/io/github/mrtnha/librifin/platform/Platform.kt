package io.github.mrtnha.librifin.platform

import kotlinx.coroutines.flow.Flow

/** Everything the shared code needs from the operating system. Implemented once per platform. */
interface Platform {
    /** Human-readable device name, shown in the Jellyfin dashboard. */
    val deviceName: String

    /** Random id generated on first launch and kept for the lifetime of the installation. */
    val deviceId: String

    val appVersion: String

    /** Local network server discovery, or null if the platform doesn't support it (yet). */
    val serverDiscovery: ServerDiscovery?

    /** Where the logged-in session (including the access token) is kept between app starts. */
    val sessionStore: SessionStore

    /** Small app settings kept between app starts, e.g. the reading theme. */
    val settingsStore: SettingsStore

    /** App-private directory for files kept until the app is uninstalled (downloaded books, the saved book list). */
    val filesDir: String

    /**
     * The libraries bundled into the app and their licenses, as AboutLibraries JSON made at build time, or null
     * if there is none. Reads a file: call it off the main thread.
     */
    fun readLicensesJson(): String?
}

/** Stores one serialized session. Must be private to the app and excluded from backups. */
interface SessionStore {
    fun read(): String?
    fun write(value: String)
    fun clear()
}

/** Stores small settings as text under a key. Nothing secret goes here. */
interface SettingsStore {
    fun read(key: String): String?
    fun write(key: String, value: String)
}

/**
 * Jellyfin UDP server discovery (https://jellyfin.org/docs/general/networking/).
 * Implementations broadcast [MESSAGE] to [PORT] repeatedly and emit the raw text of every reply,
 * until the collecting coroutine is cancelled.
 */
interface ServerDiscovery {
    fun replies(): Flow<String>

    companion object {
        const val MESSAGE = "Who is JellyfinServer?"
        const val PORT = 7359
        const val RESEND_INTERVAL_MS = 1_500L
    }
}
