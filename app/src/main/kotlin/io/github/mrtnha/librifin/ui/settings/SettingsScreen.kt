package io.github.mrtnha.librifin.ui.settings

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mrtnha.librifin.AppServices
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.ui.components.LibrifinIcons

/**
 * Settings, from the gear in the library: the account (user and server) with a way to log out, and about the
 * app (the bundled open source licenses). The app's version is at the bottom.
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
    val vm = viewModel { SettingsViewModel(session, services.jellyfin) }
    var isLogoutDialogOpen by rememberSaveable { mutableStateOf(false) }

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

                SectionTitle("About")
                ListItem(
                    headlineContent = { Text("Open source licenses") },
                    leadingContent = { Icon(LibrifinIcons.Licenses, contentDescription = null) },
                    trailingContent = { Icon(LibrifinIcons.ChevronRight, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onLicensesClick),
                )
            }
            Text(
                "Version ${services.platform.appVersion}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp),
            )
        }
    }

    // Asks first: an accidental tap would log out, and logging in again needs the password and the server.
    if (isLogoutDialogOpen) {
        AlertDialog(
            onDismissRequest = { isLogoutDialogOpen = false },
            title = { Text("Log out?") },
            text = { Text("Your downloaded books and reading progress stay on this phone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        isLogoutDialogOpen = false
                        vm.logout(onLoggedOut)
                    },
                ) {
                    Text("Log out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { isLogoutDialogOpen = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
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
