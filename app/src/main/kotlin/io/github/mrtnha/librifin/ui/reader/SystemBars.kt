package io.github.mrtnha.librifin.ui.reader

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.CancellationSignal
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationControlListenerCompat
import androidx.core.view.WindowInsetsAnimationControllerCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Shows or hides the status and navigation bars while this is in the composition, at once rather than
 * sliding, with light or dark icons to suit the background behind them ([darkBackground]).
 * They are shown again, with the app's usual icons, when it leaves.
 */
@Composable
fun SystemBarsVisible(visible: Boolean, darkBackground: Boolean) {
    val view = LocalView.current
    val window = remember(view) { view.context.findActivity()?.window } ?: return
    val controller = remember(window, view) {
        WindowCompat.getInsetsController(window, view).apply {
            // While hidden, a swipe from the edge shows the bars briefly without leaving full screen.
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    val bars = remember(controller) {
        listOf(WindowInsetsCompat.Type.statusBars(), WindowInsetsCompat.Type.navigationBars())
            .map { InstantSystemBar(controller, it) }
    }

    LaunchedEffect(bars, visible) {
        bars.forEach { it.setVisible(visible) }
    }
    DisposableEffect(controller, darkBackground) {
        val lightStatusBars = controller.isAppearanceLightStatusBars
        val lightNavigationBars = controller.isAppearanceLightNavigationBars
        controller.isAppearanceLightStatusBars = !darkBackground
        controller.isAppearanceLightNavigationBars = !darkBackground
        onDispose {
            controller.isAppearanceLightStatusBars = lightStatusBars
            controller.isAppearanceLightNavigationBars = lightNavigationBars
        }
    }
    DisposableEffect(bars) {
        onDispose { bars.forEach { it.setVisible(true) } }
    }
}

/**
 * Shows and hides one system bar ([type]) without Android's slide: it takes over the bar's animation and
 * ends it right away. Before Android 11, or when Android doesn't hand the bar over, it slides as usual.
 * One bar per request: asked for several, Android 16 waits until it has all of them, even ones the
 * device doesn't have, and never answers.
 */
private class InstantSystemBar(private val controller: WindowInsetsControllerCompat, private val type: Int) {
    /**
     * What was asked for last; Android doesn't tell. Asked for the state the bar is already in, Android
     * would flip it first, so only changes are asked for. Shown until hidden here.
     */
    private var visible = true

    /** The latest request; cancelled by the next one, so a late answer to it can't undo the next one. */
    private var pending: CancellationSignal? = null

    fun setVisible(visible: Boolean) {
        if (visible == this.visible) return
        this.visible = visible
        pending?.cancel()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            showOrHide(visible)
            return
        }
        val cancellation = CancellationSignal().also { pending = it }
        controller.controlWindowInsetsAnimation(
            type,
            0,
            null,
            cancellation,
            object : WindowInsetsAnimationControlListenerCompat {
                override fun onReady(controller: WindowInsetsAnimationControllerCompat, types: Int) {
                    controller.finish(visible)
                }

                override fun onFinished(controller: WindowInsetsAnimationControllerCompat) = Unit

                override fun onCancelled(controller: WindowInsetsAnimationControllerCompat?) {
                    // Refused by Android rather than replaced by a newer request.
                    if (!cancellation.isCanceled) showOrHide(visible)
                }
            },
        )
    }

    private fun showOrHide(visible: Boolean) {
        if (visible) controller.show(type) else controller.hide(type)
    }
}

/** The space the status and navigation bars take, also while they are hidden. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun systemBarsIgnoringVisibility(): WindowInsets = WindowInsets.systemBarsIgnoringVisibility

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
