package io.github.mrtnha.librifin.ui.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.io.files.Path

// The iOS reader (Readium swift-toolkit) comes later.
@Composable
actual fun EpubView(
    file: Path,
    initialLocator: String?,
    initialProgress: Double?,
    theme: ReaderTheme,
    jumpToProgress: Double?,
    onJumped: () -> Unit,
    onReachedEnd: () -> Unit,
    onPagesLoaded: (pages: List<BookPage>) -> Unit,
    onPositionChanged: (locator: String, progress: Double, page: Int?) -> Unit,
    onCenterTap: () -> Unit,
    onOpenFailed: (message: String) -> Unit,
    modifier: Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Reading books isn't supported on iOS yet.")
    }
}
