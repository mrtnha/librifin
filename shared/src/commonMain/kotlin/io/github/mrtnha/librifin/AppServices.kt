package io.github.mrtnha.librifin

import androidx.lifecycle.ViewModel
import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.platform.Platform

/**
 * App-wide objects, created once and shared by all screens (no DI framework).
 * A ViewModel so it survives configuration changes and is closed with the app.
 */
class AppServices(val platform: Platform) : ViewModel() {
    val jellyfin = JellyfinClient(platform)

    override fun onCleared() = jellyfin.close()
}
