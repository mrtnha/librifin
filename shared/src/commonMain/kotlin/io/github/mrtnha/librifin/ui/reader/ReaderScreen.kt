package io.github.mrtnha.librifin.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mrtnha.librifin.AppServices
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.ui.components.LibrifinIcons
import io.github.mrtnha.librifin.ui.theme.readerBarsColorScheme
import io.github.mrtnha.librifin.ui.theme.readerColorScheme
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

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
    val vm = viewModel {
        ReaderViewModel(
            session,
            bookId,
            services.jellyfin,
            services.bookStore,
            services.progressSync,
            services.platform.settingsStore,
            services.scope,
        )
    }

    // Back in the app: the book may have been read further elsewhere meanwhile.
    LifecycleEventEffect(Lifecycle.Event.ON_START) { vm.onAppResumed() }

    // Full screen only while the book is shown; while loading or on errors the bars stay, so the way back is visible.
    val showBars = vm.state !is ReaderState.Ready || vm.areBarsVisible
    val theme = vm.theme
    SystemBarsVisible(showBars, darkBackground = theme.bars.luminance() < 0.5f)
    val barsColors = readerBarsColorScheme(theme.bars)

    // The whole screen in the page's colors, also while loading; the bars stand apart in their own.
    MaterialTheme(colorScheme = readerColorScheme(theme.isDark, theme.background, theme.text)) {
        Surface(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {
                when (val state = vm.state) {
                    is ReaderState.Ready -> EpubView(
                        file = state.file,
                        initialLocator = state.startLocator,
                        initialProgress = state.startProgress,
                        theme = theme,
                        jumpToProgress = vm.jumpToProgress,
                        onJumped = vm::onJumped,
                        onReachedEnd = vm::onReachedEnd,
                        onPagesLoaded = vm::onPagesLoaded,
                        onPositionChanged = vm::onPositionChanged,
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

                // Along the bottom, with the app bar: drag to move through the whole book.
                AnimatedVisibility(
                    visible = showBars && vm.pages.size > 1,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    MaterialTheme(colorScheme = barsColors) {
                        PageSlider(pages = vm.pages, currentPage = vm.currentPage, onPageSelected = vm::jumpToPage)
                    }
                }

                AnimatedVisibility(visible = showBars, enter = fadeIn(), exit = fadeOut()) {
                    MaterialTheme(colorScheme = barsColors) {
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
                            actions = {
                                IconButton(onClick = vm::cycleTheme) {
                                    Icon(LibrifinIcons.Visibility, contentDescription = "Change theme")
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * The page slider: the chapter and "page/pages" above a slider through the whole book. While dragging,
 * they follow the finger, and the book shows that page whenever the finger rests for a moment
 * (not on every move, which would make it flicker); letting go shows it right away.
 */
@Composable
private fun PageSlider(pages: List<BookPage>, currentPage: Int?, onPageSelected: (Int) -> Unit) {
    var draggedPage by remember { mutableStateOf<Int?>(null) }
    val interactionSource = remember { MutableInteractionSource() }
    val page = (draggedPage ?: currentPage ?: 1).coerceIn(1, pages.size)

    LaunchedEffect(draggedPage) {
        val target = draggedPage ?: return@LaunchedEffect
        delay(PREVIEW_DELAY_MS)
        onPageSelected(target)
    }

    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier
                .fillMaxWidth()
                // Like the app bar: room for the navigation bar even while it's hidden, so nothing jumps.
                .windowInsetsPadding(
                    systemBarsIgnoringVisibility().only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                )
                .padding(start = 24.dp, end = 24.dp, top = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    pages[page - 1].chapter.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "$page/${pages.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
            Slider(
                value = page.toFloat(),
                onValueChange = { draggedPage = it.roundToInt() },
                onValueChangeFinished = {
                    draggedPage?.let(onPageSelected)
                    draggedPage = null
                },
                valueRange = 1f..pages.size.toFloat(),
                interactionSource = interactionSource,
                // A shorter handle than Material's 44 dp, to keep the bar calm.
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = interactionSource,
                        thumbSize = DpSize(4.dp, 32.dp),
                    )
                },
            )
        }
    }
}

/** How long the finger has to rest on the page slider before the book shows that page. */
private const val PREVIEW_DELAY_MS = 50L
