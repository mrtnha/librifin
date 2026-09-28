package io.github.mrtnha.librifin.ui.server

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mrtnha.librifin.AppServices
import io.github.mrtnha.librifin.api.Server
import io.github.mrtnha.librifin.ui.components.FlowScaffold
import io.github.mrtnha.librifin.ui.components.LibrifinIcons
import io.github.mrtnha.librifin.ui.components.ProgressRow
import io.github.mrtnha.librifin.ui.components.ServerCard

@Composable
fun ServerSelectionScreen(
    services: AppServices,
    onBack: () -> Unit,
    onServerSelected: (Server) -> Unit,
) {
    val vm = viewModel { ServerSelectionViewModel(services.jellyfin, services.platform.serverDiscovery) }

    // Discover only while this screen is shown, like Finamp.
    DisposableEffect(vm) {
        vm.startDiscovery()
        onDispose { vm.stopDiscovery() }
    }

    FlowScaffold(title = "Connect to Jellyfin", onBack = onBack) {
        if (vm.isDiscoverySupported) {
            SectionTitle("Servers on your local network")
            vm.discoveredServers.forEach { server ->
                ServerCard(server, onClick = { onServerSelected(server) }, modifier = Modifier.padding(bottom = 8.dp))
            }
            ProgressRow("Scanning for servers…", Modifier.padding(vertical = 12.dp))
            if (vm.isNothingFound && vm.discoveredServers.isEmpty()) {
                Text(
                    "No servers found yet. Make sure you're on the same Wi-Fi as your Jellyfin server, " +
                        "or enter its address below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            if (!vm.isManualEntryVisible) {
                TextButton(onClick = vm::showManualEntry, modifier = Modifier.padding(top = 4.dp)) {
                    Text("Server not listed? Click here to enter its address")
                }
            }
        }

        // Fades in where the link was, instead of sliding open.
        AnimatedVisibility(visible = vm.isManualEntryVisible, enter = fadeIn(), exit = fadeOut()) {
            ManualEntry(vm, onServerSelected, requestFocus = vm.isDiscoverySupported)
        }
    }
}

@Composable
private fun ManualEntry(vm: ServerSelectionViewModel, onServerSelected: (Server) -> Unit, requestFocus: Boolean) {
    Column(Modifier.fillMaxWidth()) {
        SectionTitle("Server address")
        ServerUrlField(value = vm.urlInput, onValueChange = vm::onUrlChanged, requestFocus = requestFocus)

        Column(Modifier.fillMaxWidth().heightIn(min = 80.dp).padding(top = 12.dp)) {
            val manual = vm.manualServer
            val error = vm.urlError
            when {
                manual != null -> ServerCard(manual, onClick = { onServerSelected(manual) })
                vm.isTestingUrl -> ProgressRow("Connecting to server…")
                error != null -> Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
    )
}

@Composable
private fun ServerUrlField(value: String, onValueChange: (String) -> Unit, requestFocus: Boolean) {
    var showInfo by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    if (requestFocus) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        placeholder = { Text("e.g. 192.168.1.10") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Done,
        ),
        trailingIcon = {
            IconButton(onClick = { showInfo = true }) {
                Icon(LibrifinIcons.Info, contentDescription = "About the server address")
            }
        },
    )

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("OK") } },
            text = {
                Text(
                    "At home, the local address of your server works (for example 192.168.1.10). " +
                        "To connect from outside your network, use its external address or domain.\n\n" +
                        "If your server uses the default ports (80, 443 or Jellyfin's 8096), you don't need to " +
                        "enter the port.\n\nOnce the address is right, your server appears below the field."
                )
            },
        )
    }
}
