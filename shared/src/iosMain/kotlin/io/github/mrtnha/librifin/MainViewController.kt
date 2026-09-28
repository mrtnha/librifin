package io.github.mrtnha.librifin

import androidx.compose.ui.window.ComposeUIViewController
import io.github.mrtnha.librifin.platform.IosPlatform

fun MainViewController() = ComposeUIViewController {
    App(IosPlatform())
}
