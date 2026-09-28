package io.github.mrtnha.librifin.platform

import platform.Foundation.NSBundle
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIDevice

class IosPlatform : Platform {
    override val deviceName: String = UIDevice.currentDevice.name

    override val deviceId: String = run {
        val defaults = NSUserDefaults.standardUserDefaults
        defaults.stringForKey(KEY_DEVICE_ID) ?: NSUUID().UUIDString.also { defaults.setObject(it, KEY_DEVICE_ID) }
    }

    override val appVersion: String =
        NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "0"

    // Needs the local network permission and the multicast entitlement; manual entry only for now.
    override val serverDiscovery: ServerDiscovery? = null

    override val sessionStore: SessionStore = IosSessionStore()

    override val settingsStore: SettingsStore = IosSettingsStore()

    override val filesDir: String =
        NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true).first() as String

    private companion object {
        const val KEY_DEVICE_ID = "device_id"
    }
}
