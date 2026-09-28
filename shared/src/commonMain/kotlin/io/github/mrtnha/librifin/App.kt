package io.github.mrtnha.librifin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import io.github.mrtnha.librifin.navigation.Navigator
import io.github.mrtnha.librifin.navigation.Screen
import io.github.mrtnha.librifin.ui.server.ServerSelectionScreen
import io.github.mrtnha.librifin.ui.welcome.WelcomeScreen

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
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
            ) { screen ->
                when (screen) {
                    Screen.Welcome -> WelcomeScreen(onGetStarted = { navigator.push(Screen.ServerSelection) })
                    Screen.ServerSelection -> ServerSelectionScreen(onBack = navigator::pop)
                }
            }
        }
    }
}
