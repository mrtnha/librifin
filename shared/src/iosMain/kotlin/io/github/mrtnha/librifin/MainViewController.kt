package io.github.mrtnha.librifin

import androidx.compose.ui.window.ComposeUIViewController
import io.github.mrtnha.librifin.platform.IosPlatform

/** One per app process, see [AppServices]. */
private val services by lazy { AppServices(IosPlatform()) }

fun MainViewController() = ComposeUIViewController {
    App(services)
}
