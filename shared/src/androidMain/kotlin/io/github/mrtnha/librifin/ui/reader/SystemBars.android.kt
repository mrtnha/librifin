package io.github.mrtnha.librifin.ui.reader

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
actual fun SystemBarsVisible(visible: Boolean) {
    val view = LocalView.current
    val window = remember(view) { view.context.findActivity()?.window } ?: return
    val controller = remember(window, view) {
        WindowCompat.getInsetsController(window, view).apply {
            // While hidden, a swipe from the edge shows the bars briefly without leaving full screen.
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    LaunchedEffect(controller, visible) {
        if (visible) {
            controller.show(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }
    DisposableEffect(controller) {
        onDispose { controller.show(WindowInsetsCompat.Type.systemBars()) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
actual fun systemBarsIgnoringVisibility(): WindowInsets = WindowInsets.systemBarsIgnoringVisibility

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
