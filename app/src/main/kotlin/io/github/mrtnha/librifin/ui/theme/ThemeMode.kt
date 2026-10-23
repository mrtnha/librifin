package io.github.mrtnha.librifin.ui.theme

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.mrtnha.librifin.storage.SettingsStore

/** The app's colors, chosen in the settings, which list them in this order. */
enum class ThemeMode {
    LIGHT,
    DARK,

    /** Dark with pure black backgrounds: an OLED screen switches those pixels off. */
    BLACK,

    /** Light or dark, like the phone. The default. */
    SYSTEM,

    /** Light or black, like the phone. */
    SYSTEM_BLACK,
}

/** Whether this mode shows dark colors, given whether the phone is in dark mode ([phoneIsDark]). */
fun ThemeMode.isDark(phoneIsDark: Boolean): Boolean = when (this) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK, ThemeMode.BLACK -> true
    ThemeMode.SYSTEM, ThemeMode.SYSTEM_BLACK -> phoneIsDark
}

/** Whether this mode shows black backgrounds, given whether the phone is in dark mode ([phoneIsDark]). */
fun ThemeMode.isBlack(phoneIsDark: Boolean): Boolean = when (this) {
    ThemeMode.BLACK -> true
    ThemeMode.SYSTEM_BLACK -> phoneIsDark
    else -> false
}

/** The night mode Android uses for the app's own windows, such as the launch screen (Android 12 and later). */
val ThemeMode.nightMode: Int
    get() = when (this) {
        ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
        ThemeMode.DARK, ThemeMode.BLACK -> UiModeManager.MODE_NIGHT_YES
        ThemeMode.SYSTEM, ThemeMode.SYSTEM_BLACK -> UiModeManager.MODE_NIGHT_AUTO
    }

/** The mode saved as [stored] (its name), or [ThemeMode.SYSTEM] if none or an unknown one is saved. */
fun themeModeOf(stored: String?): ThemeMode = ThemeMode.entries.find { it.name == stored } ?: ThemeMode.SYSTEM

/**
 * Reads and switches the [ThemeMode]. A change shows at once: [mode] is Compose state, which the app's theme and
 * the activity's window (MainActivity) follow.
 */
class AppTheme(context: Context, private val settings: SettingsStore) {
    private val uiModeManager = context.applicationContext.getSystemService(UiModeManager::class.java)

    var mode: ThemeMode by mutableStateOf(themeModeOf(settings.read(KEY_MODE)))
        private set

    /**
     * Saves and shows [mode]. From Android 12 on, Android also learns the app's night mode and keeps it, so the
     * launch screen and what Android draws for the app (such as the text selection menu) match it. Before
     * Android 12 those follow the phone.
     */
    fun select(mode: ThemeMode) {
        settings.write(KEY_MODE, mode.name)
        this.mode = mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) uiModeManager.setApplicationNightMode(mode.nightMode)
    }

    private companion object {
        const val KEY_MODE = "app_theme"
    }
}
