package io.github.mrtnha.librifin.platform

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import io.github.mrtnha.librifin.R
import java.util.UUID

/** Everything the app needs from Android. */
class Platform(context: Context) {
    private val appContext = context.applicationContext

    // App-private file. Excluded from cloud backup and device transfer (see res/xml):
    // it holds the access token, and a restored device id would make two phones one device to Jellyfin.
    private val prefs = appContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    /** Human-readable device name, shown in the Jellyfin dashboard. */
    val deviceName: String =
        Settings.Global.getString(appContext.contentResolver, Settings.Global.DEVICE_NAME)
            ?.takeIf { it.isNotBlank() }
            ?: Build.MODEL

    /** Random id generated on first launch and kept for the lifetime of the installation. */
    val deviceId: String = run {
        prefs.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_DEVICE_ID, it).apply()
        }
    }

    val appVersion: String =
        appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName ?: "0"

    val serverDiscovery = ServerDiscovery(appContext)

    /** Where the logged-in session (including the access token) is kept between app starts. */
    val sessionStore = SessionStore(prefs)

    /** Small app settings kept between app starts, e.g. the reading theme. */
    val settingsStore = SettingsStore(prefs)

    /** App-private directory for files kept until the app is uninstalled (downloaded books, the saved book list). */
    val filesDir: String = appContext.filesDir.absolutePath

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

/** Stores one serialized session. */
class SessionStore(private val prefs: SharedPreferences) {
    fun read(): String? = prefs.getString(KEY_SESSION, null)
    fun write(value: String) = prefs.edit().putString(KEY_SESSION, value).apply()
    fun clear() = prefs.edit().remove(KEY_SESSION).apply()

    private companion object {
        const val KEY_SESSION = "session"
    }
}

/** Stores small settings as text under a key. Nothing secret goes here. */
class SettingsStore(private val prefs: SharedPreferences) {
    fun read(key: String): String? = prefs.getString(key, null)
    fun write(key: String, value: String) = prefs.edit().putString(key, value).apply()
}
