package io.github.mrtnha.librifin.ui.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.io.files.Path

// The iOS reader (Readium swift-toolkit) comes later.
@Composable
actual fun EpubView(file: Path, initialLocator: String?, host: EpubViewHost, modifier: Modifier) {
    // Nothing to search in, so every search is done right away.
    LaunchedEffect(host.searchedQuery) { if (host.searchedQuery != null) host.onSearchResults(emptyList(), true) }
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Reading books isn't supported on iOS yet.")
    }
}
