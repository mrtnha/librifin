package io.github.mrtnha.librifin.platform

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.util.UUID

class AndroidPlatform(context: Context) : Platform {
    private val appContext = context.applicationContext

    override val deviceName: String =
        (if (Build.VERSION.SDK_INT >= 25) Settings.Global.getString(appContext.contentResolver, Settings.Global.DEVICE_NAME) else null)
            ?.takeIf { it.isNotBlank() }
            ?: Build.MODEL

    override val deviceId: String = run {
        val prefs = appContext.getSharedPreferences("librifin", Context.MODE_PRIVATE)
        prefs.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_DEVICE_ID, it).apply()
        }
    }

    override val appVersion: String =
        appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName ?: "0"

    override val serverDiscovery: ServerDiscovery = AndroidServerDiscovery(appContext)

    private companion object {
        const val KEY_DEVICE_ID = "device_id"
    }
}
