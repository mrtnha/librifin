package io.github.mrtnha.librifin

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * The app's icon on the home screen and in the app drawer, chosen in the settings, which list them in this order.
 * Each one has its own launcher entry in AndroidManifest.xml, an activity-alias of MainActivity, and exactly one
 * entry is switched on. [entry] is its full name: the manifest's ".DarkIcon" is completed with the namespace, not
 * with the application id.
 */
enum class AppIcon(val entry: String) {
    /** The book on white. */
    LIGHT("io.github.mrtnha.librifin.LightIcon"),

    /** The book on Jellyfin's dark navy. Its entry is the one switched on after installing. */
    DARK("io.github.mrtnha.librifin.DarkIcon"),

    /** The book on pure black, like the black app theme. */
    BLACK("io.github.mrtnha.librifin.BlackIcon"),
}

/**
 * Reads and switches the [AppIcon]. Android keeps which launcher entry is switched on, also across app updates,
 * so the choice isn't saved anywhere else and can't disagree with the icon on the home screen.
 */
class AppIcons(context: Context) {
    private val appContext = context.applicationContext

    /**
     * The icon whose entry is switched on. Until another icon is chosen, that's DARK's by the manifest's default
     * (AndroidManifest.xml), which isn't switched on explicitly; LIGHT's and BLACK's are off until they're chosen.
     */
    val current: AppIcon
        get() = AppIcon.entries.firstOrNull {
            appContext.packageManager.getComponentEnabledSetting(component(it)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } ?: AppIcon.DARK

    /**
     * Switches the entry of [icon] on and the others off. The launcher shows the new icon about a second later.
     * Android then also closes the app if it was opened through an entry that's now off, so the caller closes it
     * right away instead (see SettingsScreen).
     */
    fun switchTo(icon: AppIcon) {
        // The new entry first, so there's always one.
        setEnabled(icon, true)
        AppIcon.entries.filter { it != icon }.forEach { setEnabled(it, false) }
    }

    /**
     * Explicitly on or off rather than back to the manifest's default, so the choice also holds if a later version
     * changes that default. DONT_KILL_APP: without it Android ends the app at once, cutting off whatever is still
     * being saved or sent to Jellyfin.
     */
    private fun setEnabled(icon: AppIcon, enabled: Boolean) {
        val state = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        appContext.packageManager.setComponentEnabledSetting(component(icon), state, PackageManager.DONT_KILL_APP)
    }

    private fun component(icon: AppIcon) = ComponentName(appContext, icon.entry)
}
