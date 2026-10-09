package io.github.mrtnha.librifin.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.mikepenz.aboutlibraries.entity.Library as BundledLibrary
import io.github.mrtnha.librifin.api.Server
import io.github.mrtnha.librifin.api.Session

/** Every screen of the app. Screens that need data carry it as properties. */
sealed interface Screen {
    data object Welcome : Screen
    data object ServerSelection : Screen
    /** [username] prefills the form, e.g. when the saved session has expired. */
    data class Login(val server: Server, val username: String = "") : Screen
    data class Library(val session: Session) : Screen
    data class Reader(val session: Session, val bookId: String, val title: String) : Screen
    data object Licenses : Screen
    /** The license of one library, font or icon set bundled into the app. */
    data class License(val library: BundledLibrary) : Screen
}

/**
 * One screen on the back stack. It owns the ViewModels of that screen, which are cleared
 * (and their coroutines cancelled) when the entry leaves the stack.
 */
class BackStackEntry(val screen: Screen) : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
}

/**
 * Minimal state-based navigation: a back stack of [BackStackEntry]s.
 * It is a ViewModel so the stack survives configuration changes such as rotation.
 */
class Navigator(start: Screen) : ViewModel() {
    private val backStack = mutableStateListOf(BackStackEntry(start))

    val current: BackStackEntry get() = backStack.last()
    val canGoBack: Boolean get() = backStack.size > 1

    /** True if the last change moved forward (push/replace), false if it went back. Used for transitions. */
    var isForward by mutableStateOf(true)
        private set

    fun push(screen: Screen) {
        isForward = true
        backStack.add(BackStackEntry(screen))
    }

    fun pop() {
        if (!canGoBack) return
        isForward = false
        backStack.removeAt(backStack.lastIndex).viewModelStore.clear()
    }

    /**
     * Replaces the whole stack with [screens] (the last one is shown), e.g. after login so back
     * doesn't return to the login flow.
     */
    fun replaceAll(vararg screens: Screen) {
        require(screens.isNotEmpty())
        isForward = true
        val old = backStack.toList()
        backStack.clear()
        screens.mapTo(backStack) { BackStackEntry(it) }
        old.forEach { it.viewModelStore.clear() }
    }

    override fun onCleared() {
        backStack.forEach { it.viewModelStore.clear() }
    }
}
