package io.github.mrtnha.librifin.ui.settings

import android.content.Context
import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mrtnha.librifin.AppServices
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.storage.Downloads
import io.github.mrtnha.librifin.ui.components.LibrifinIcons

/**
 * Settings, from the gear in the library: the account (user and server) with a way to log out, the downloaded
 * books with a way to remove them, and about the app (the bundled open source licenses). The app's version is
 * at the bottom.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    session: Session,
    services: AppServices,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onLicensesClick: () -> Unit,
) {
    val vm = viewModel { SettingsViewModel(session, services.jellyfin, services.bookStore) }
    var isLogoutDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isRemoveDownloadsDialogOpen by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(LibrifinIcons.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                SectionTitle("Account")
                ListItem(
                    headlineContent = { Text(session.userName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingContent = { Icon(LibrifinIcons.Profile, contentDescription = null) },
                )
                ListItem(
                    headlineContent = {
                        Text(session.server.baseUrl, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    leadingContent = { Icon(LibrifinIcons.Server, contentDescription = null) },
                )
                ListItem(
                    headlineContent = { Text("Log out", color = MaterialTheme.colorScheme.error) },
                    leadingContent = {
                        if (vm.isLoggingOut) {
                            // In the icon's place and size, so the text doesn't move.
                            CircularProgressIndicator(Modifier.size(24.dp).padding(2.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                LibrifinIcons.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                    modifier = Modifier.clickable(enabled = !vm.isLoggingOut) { isLogoutDialogOpen = true },
                )

                SectionTitle("Downloads")
                val downloads = vm.downloads
                val hasDownloads = downloads != null && downloads.bookCount > 0
                // Grayed out while there's nothing to remove.
                val removeColor = if (hasDownloads) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_ALPHA)
                }
                val context = LocalContext.current
                ListItem(
                    headlineContent = { Text("Remove downloads", color = removeColor) },
                    // Empty while counting, so the row doesn't change its height when the count arrives.
                    supportingContent = { Text(downloads?.let { downloadsSummary(it, context) }.orEmpty()) },
                    leadingContent = { Icon(LibrifinIcons.Delete, contentDescription = null, tint = removeColor) },
                    modifier = Modifier.clickable(enabled = hasDownloads) { isRemoveDownloadsDialogOpen = true },
                )

                SectionTitle("About")
                ListItem(
                    headlineContent = { Text("Open source licenses") },
                    leadingContent = { Icon(LibrifinIcons.Licenses, contentDescription = null) },
                    trailingContent = { Icon(LibrifinIcons.ChevronRight, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onLicensesClick),
                )
            }
            Text(
                "Version ${services.appVersion}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp),
            )
        }
    }

    // Both ask first: an accidental logout means logging in again, with the password and the server; accidentally
    // removed books have to be downloaded again, which needs the server too.
    if (isLogoutDialogOpen) {
        ConfirmDialog(
            title = "Log out?",
            text = "Your downloaded books and reading progress stay on this phone.",
            confirmLabel = "Log out",
            onConfirm = { vm.logout(onLoggedOut) },
            onDismiss = { isLogoutDialogOpen = false },
        )
    }
    if (isRemoveDownloadsDialogOpen) {
        ConfirmDialog(
            title = "Remove downloads?",
            text = "Your reading progress stays. Books download again when you open them.",
            confirmLabel = "Remove",
            onConfirm = vm::removeDownloads,
            onDismiss = { isRemoveDownloadsDialogOpen = false },
        )
    }
}

/** A group's title: small, gray and in capitals, at the left edge like the icons below it. */
@Composable
private fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
    )
}

/** Asks before [onConfirm]: [confirmLabel] in red, Cancel in the normal text color. Both close the dialog. */
@Composable
private fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onConfirm()
                },
            ) {
                Text(confirmLabel, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
            }
        },
    )
}

/** "12 books · 34 MB", with the size the way Android's own settings show it, or that there are none. */
private fun downloadsSummary(downloads: Downloads, context: Context): String {
    if (downloads.bookCount == 0) return "No books downloaded"
    val books = if (downloads.bookCount == 1) "1 book" else "${downloads.bookCount} books"
    return "$books · ${Formatter.formatShortFileSize(context, downloads.bytes)}"
}

/** Material's opacity for disabled content. */
private const val DISABLED_ALPHA = 0.38f
