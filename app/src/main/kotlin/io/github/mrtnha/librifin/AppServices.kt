package io.github.mrtnha.librifin

import android.content.Context
import android.os.Build
import android.provider.Settings
import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.ServerDiscovery
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.api.decodeOrNull
import io.github.mrtnha.librifin.storage.BookStore
import io.github.mrtnha.librifin.storage.SessionStore
import io.github.mrtnha.librifin.storage.SettingsStore
import io.github.mrtnha.librifin.sync.ProgressSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.UUID

/**
 * App-wide objects, shared by all screens (no DI framework). Create exactly one per app process
 * (in LibrifinApplication), not per activity: a second
 * [BookStore] would no longer keep the saves of the first in order. Lives as long as the process,
 * so nothing here is ever closed or cancelled. Everything the app needs from Android comes in through [context].
 */
class AppServices(context: Context) {
    private val appContext = context.applicationContext

    // App-private file. Excluded from cloud backup and device transfer (see res/xml):
    // it holds the access token, and a restored device id would make two phones one device to Jellyfin.
    private val prefs = appContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    /** Human-readable device name, shown in the Jellyfin dashboard. */
    private val deviceName: String =
        Settings.Global.getString(appContext.contentResolver, Settings.Global.DEVICE_NAME)
            ?.takeIf { it.isNotBlank() }
            ?: Build.MODEL

    /** Random id generated on first launch and kept for the lifetime of the installation. */
    private val deviceId: String = run {
        prefs.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_DEVICE_ID, it).apply()
        }
    }

    val appVersion: String =
        appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName ?: "0"

    val jellyfin = JellyfinClient(deviceName = deviceName, deviceId = deviceId, appVersion = appVersion)

    /** Downloaded books and the saved book list, in an app-private directory kept until the app is uninstalled. */
    val bookStore = BookStore(appContext.filesDir.absolutePath)

    val serverDiscovery = ServerDiscovery(appContext)

    /** Small app settings kept between app starts, e.g. the reading theme. */
    val settingsStore = SettingsStore(prefs)

    /** Where the logged-in session (including the access token) is kept between app starts. */
    private val sessionStore = SessionStore(prefs)

    /**
     * For work that must finish even if the screen that started it is closed, e.g. saving the reading
     * position. Never cancelled, so it also outlives the activity when the app is left right away.
     */
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val progressSync = ProgressSync(jellyfin, bookStore, scope)

    /** The session saved at the last login, or null if logged out (or the saved data is unreadable). */
    fun loadSession(): Session? {
        val saved = sessionStore.read() ?: return null
        val session = JellyfinClient.json.decodeOrNull<Session>(saved)
        if (session == null) sessionStore.clear()
        return session
    }

    fun saveSession(session: Session) = sessionStore.write(JellyfinClient.json.encodeToString(session))

    fun clearSession() = sessionStore.clear()

    /**
     * The libraries bundled into the app and their licenses, as AboutLibraries JSON made at build time.
     * Reads a file: call it off the main thread.
     */
    fun readLicensesJson(): String =
        appContext.resources.openRawResource(R.raw.aboutlibraries).bufferedReader().use { it.readText() }

    private companion object {
        const val PREFS_FILE = "librifin" // → shared_prefs/librifin.xml
        const val KEY_DEVICE_ID = "device_id"
    }
}
