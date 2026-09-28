package io.github.mrtnha.librifin.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mrtnha.librifin.AppServices
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.ui.components.LibrifinIcons

/** Shows one book full screen. The app bar lies on top of the book, so the book doesn't move when it appears. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    session: Session,
    bookId: String,
    title: String,
    services: AppServices,
    onBack: () -> Unit,
) {
    val vm = viewModel { ReaderViewModel(session, bookId, services.jellyfin, services.platform.cacheDir) }

    // Full screen only while the book is shown; while loading or on errors the bars stay, so the way back is visible.
    val showBars = vm.state !is ReaderState.Ready || vm.areBarsVisible
    SystemBarsVisible(showBars)

    Box(Modifier.fillMaxSize()) {
        when (val state = vm.state) {
            is ReaderState.Ready -> EpubView(
                file = state.file,
                onCenterTap = vm::toggleBars,
                onOpenFailed = vm::onOpenFailed,
                modifier = Modifier.fillMaxSize(),
            )
            else -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                when (state) {
                    is ReaderState.Downloading ->
                        if (state.progress == null) {
                            CircularProgressIndicator()
                        } else {
                            CircularProgressIndicator(progress = { state.progress })
                        }
                    is ReaderState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, textAlign = TextAlign.Center)
                        if (state.canRetry) {
                            TextButton(onClick = vm::load, modifier = Modifier.padding(top = 8.dp)) { Text("Try again") }
                        }
                    }
                    is ReaderState.Ready -> Unit
                }
            }
        }

        AnimatedVisibility(visible = showBars, enter = fadeIn(), exit = fadeOut()) {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                // Reserve room for the status bar even while it's hidden, so the app bar doesn't jump
                // down once the status bar has finished appearing.
                windowInsets = systemBarsIgnoringVisibility()
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(LibrifinIcons.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    }
}
