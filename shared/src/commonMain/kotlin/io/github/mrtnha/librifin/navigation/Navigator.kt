package io.github.mrtnha.librifin.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/** Every screen of the app. Screens that need data carry it as properties. */
sealed interface Screen {
    data object Welcome : Screen
    data object ServerSelection : Screen
}

/**
 * Minimal state-based navigation: a back stack of [Screen]s.
 * It is a ViewModel so the stack survives configuration changes such as rotation.
 */
class Navigator(start: Screen) : ViewModel() {
    private val backStack = mutableStateListOf(start)

    val current: Screen get() = backStack.last()
    val canGoBack: Boolean get() = backStack.size > 1

    /** True if the last change moved forward (push/replace), false if it went back. Used for transitions. */
    var isForward by mutableStateOf(true)
        private set

    fun push(screen: Screen) {
        isForward = true
        backStack.add(screen)
    }

    fun pop() {
        if (!canGoBack) return
        isForward = false
        backStack.removeAt(backStack.lastIndex)
    }

    /** Clears the stack and shows [screen], e.g. after login so back doesn't return to the login flow. */
    fun replaceAll(screen: Screen) {
        isForward = true
        backStack.clear()
        backStack.add(screen)
    }
}
