package io.github.mrtnha.librifin

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.FragmentActivity
import io.github.mrtnha.librifin.ui.reader.ReaderFragmentFactory
import io.github.mrtnha.librifin.ui.theme.AppTheme
import io.github.mrtnha.librifin.ui.theme.appColorScheme
import io.github.mrtnha.librifin.ui.theme.isBlack
import io.github.mrtnha.librifin.ui.theme.isDark

// A FragmentActivity because Readium's EPUB renderer is a Fragment.
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = ReaderFragmentFactory
        val appContainer = (application as LibrifinApplication).appContainer
        val appTheme = appContainer.appTheme
        enableEdgeToEdge(appTheme)
        super.onCreate(savedInstanceState)
        ReaderFragmentFactory.removeOrphans(supportFragmentManager)

        setContent {
            // A change of the theme or of the phone's dark mode shows without recreating the activity (see
            // AndroidManifest.xml), so the window follows it here.
            val mode = appTheme.mode
            val background = appColorScheme(mode, isSystemInDarkTheme()).background
            // Only for a new mode: a change of the phone's dark mode is a configuration change, after which
            // enableEdgeToEdge looks again itself, before the reader puts its own icons on top (SystemBars.kt).
            LaunchedEffect(mode) {
                enableEdgeToEdge(appTheme)
                // Android keeps this theme for the next launch screens. Without it they show the dark background,
                // not black. Before Android 12 the launch screen follows the phone.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    splashScreen.setSplashScreenTheme(
                        if (mode.isBlack(phoneIsDark = true)) R.style.Theme_Librifin_Black else Resources.ID_NULL,
                    )
                }
            }
            // Shows where Compose hasn't drawn yet.
            LaunchedEffect(background) {
                window.setBackgroundDrawable(background.toArgb().toDrawable())
            }
            App(appContainer)
        }
    }
}

/**
 * Draws the app behind the status and navigation bars, with their icons light or dark to suit the app's theme
 * rather than the phone's. enableEdgeToEdge calls `isDark` again on each configuration change, so it reads the
 * current mode.
 * The scrims are only for the navigation bar on Android 8 and 9: from Android 10 on, Android keeps its buttons
 * readable itself.
 */
private fun ComponentActivity.enableEdgeToEdge(appTheme: AppTheme) {
    val isDark = { resources: Resources ->
        val night = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        appTheme.mode.isDark(phoneIsDark = night == Configuration.UI_MODE_NIGHT_YES)
    }
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT, isDark),
        navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM, isDark),
    )
}

/** The see-through white and gray of enableEdgeToEdge's own navigation bar style. */
private const val LIGHT_SCRIM = 0xE6FFFFFF.toInt()
private const val DARK_SCRIM = 0x801B1B1B.toInt()
