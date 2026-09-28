package io.github.mrtnha.librifin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import io.github.mrtnha.librifin.navigation.Navigator
import io.github.mrtnha.librifin.navigation.Screen
import io.github.mrtnha.librifin.platform.Platform
import io.github.mrtnha.librifin.ui.library.LibraryScreen
import io.github.mrtnha.librifin.ui.login.LoginScreen
import io.github.mrtnha.librifin.ui.server.ServerSelectionScreen
import io.github.mrtnha.librifin.ui.theme.LibrifinTheme
import io.github.mrtnha.librifin.ui.welcome.WelcomeScreen

@Composable
fun App(platform: Platform) {
    // Covers are loaded with Coil over Ktor (coil-network-ktor3 registers itself); fade them in.
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context).crossfade(true).build()
    }

    LibrifinTheme {
        val services = viewModel { AppServices(platform) }
        val navigator = viewModel { Navigator(Screen.Welcome) }

        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            isBackEnabled = navigator.canGoBack,
            onBackCompleted = navigator::pop,
        )

        Surface(Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = navigator.current,
                transitionSpec = {
                    val direction = if (navigator.isForward) 1 else -1
                    slideInHorizontally { width -> direction * width } togetherWith
                        slideOutHorizontally { width -> -direction * width }
                },
            ) { entry ->
                // ViewModels created inside a screen belong to its back stack entry.
                CompositionLocalProvider(LocalViewModelStoreOwner provides entry) {
                    when (val screen = entry.screen) {
                        Screen.Welcome -> WelcomeScreen(
                            onGetStarted = { navigator.push(Screen.ServerSelection) },
                        )
                        Screen.ServerSelection -> ServerSelectionScreen(
                            services = services,
                            onBack = navigator::pop,
                            onServerSelected = { navigator.push(Screen.Login(it)) },
                        )
                        is Screen.Login -> LoginScreen(
                            server = screen.server,
                            services = services,
                            onBack = navigator::pop,
                            onLoggedIn = { navigator.replaceAll(Screen.Library(it)) },
                        )
                        is Screen.Library -> LibraryScreen(session = screen.session, services = services)
                    }
                }
            }
        }
    }
}
