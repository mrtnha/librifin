package io.github.mrtnha.librifin.platform

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.util.UUID

/**
 * [licensesJsonReader] reads the license list, which the app module generates as a raw resource. Only the app
 * module knows its id.
 */
class AndroidPlatform(context: Context, private val licensesJsonReader: () -> String) : Platform {
    private val appContext = context.applicationContext

    // App-private file. Excluded from cloud backup and device transfer (see res/xml in androidApp):
    // it holds the access token, and a restored device id would make two phones one device to Jellyfin.
    private val prefs = appContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    override val deviceName: String =
        Settings.Global.getString(appContext.contentResolver, Settings.Global.DEVICE_NAME)
            ?.takeIf { it.isNotBlank() }
            ?: Build.MODEL

    override val deviceId: String = run {
        prefs.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_DEVICE_ID, it).apply()
        }
    }

    override val appVersion: String =
        appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName ?: "0"

    override val serverDiscovery: ServerDiscovery = AndroidServerDiscovery(appContext)

    override val sessionStore: SessionStore = object : SessionStore {
        override fun read(): String? = prefs.getString(KEY_SESSION, null)
        override fun write(value: String) = prefs.edit().putString(KEY_SESSION, value).apply()
        override fun clear() = prefs.edit().remove(KEY_SESSION).apply()
    }

    override val settingsStore: SettingsStore = object : SettingsStore {
        override fun read(key: String): String? = prefs.getString(key, null)
        override fun write(key: String, value: String) = prefs.edit().putString(key, value).apply()
    }

    override val filesDir: String = appContext.filesDir.absolutePath

    override fun readLicensesJson(): String = licensesJsonReader()

    private companion object {
        const val PREFS_FILE = "librifin" // → shared_prefs/librifin.xml
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_SESSION = "session"
    }
}
