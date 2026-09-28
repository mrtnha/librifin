package io.github.mrtnha.librifin.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import io.github.mrtnha.librifin.AppServices
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.ui.components.LibrifinIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    session: Session,
    services: AppServices,
    onLoggedOut: () -> Unit,
    onSessionExpired: () -> Unit,
) {
    val vm = viewModel { LibraryViewModel(session, services.jellyfin) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Librifin", fontWeight = FontWeight.SemiBold) },
                actions = {
                    // Search isn't wired up yet.
                    IconButton(onClick = {}) {
                        Icon(LibrifinIcons.Search, contentDescription = "Search books")
                    }
                    IconButton(onClick = { vm.isProfileSheetOpen = true }) {
                        Icon(LibrifinIcons.Profile, contentDescription = "Profile")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
            when (val state = vm.state) {
                LibraryState.Loading -> CircularProgressIndicator()
                LibraryState.NoBookLibrary -> Message("No book library found on this server.")
                is LibraryState.Error ->
                    if (state.isSessionExpired) {
                        Message(state.message, actionLabel = "Log in again", onAction = onSessionExpired)
                    } else {
                        Message(state.message, actionLabel = "Try again", onAction = vm::load)
                    }
                is LibraryState.Loaded ->
                    if (state.books.isEmpty()) Message("No books yet.") else BookGrid(state.books)
            }
        }
    }

    if (vm.isProfileSheetOpen) {
        ProfileSheet(
            session = session,
            userImageUrl = services.jellyfin.userImageUrl(session),
            isLoggingOut = vm.isLoggingOut,
            onLogout = { vm.logout(onLoggedOut) },
            onDismiss = { vm.isProfileSheetOpen = false },
        )
    }
}

@Composable
private fun BookGrid(books: List<Book>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(books, key = { it.id }) { book ->
            BookItem(book)
        }
    }
}

@Composable
private fun BookItem(book: Book) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            // Placeholder, visible while the cover loads or if there is none.
            Icon(
                LibrifinIcons.Book,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp),
            )
            if (book.coverUrl != null) {
                AsyncImage(
                    model = book.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Text(
            book.title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun Message(text: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, modifier = Modifier.padding(top = 8.dp)) { Text(actionLabel) }
        }
    }
}
