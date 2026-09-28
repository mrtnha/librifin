package io.github.mrtnha.librifin.ui.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mrtnha.librifin.AppServices
import io.github.mrtnha.librifin.api.Server
import io.github.mrtnha.librifin.ui.components.FlowHeader
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
    ) {
        FlowHeader(title = "Connect to Jellyfin", backLabel = "Back", onBack = onBack)

        Text(
            "Server URL",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
        ServerUrlField(value = vm.urlInput, onValueChange = vm::onUrlChanged)

        // Result of the manual entry
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 95.dp).padding(top = 12.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            val manual = vm.manualServer
            when {
                manual != null -> ServerCard(manual, onClick = { onServerSelected(manual) })
                vm.isTestingUrl -> ProgressRow("Connecting to server…")
                vm.urlError != null -> Text(
                    vm.urlError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        }

        Text(
            "Servers on your local network:",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 16.dp),
        )
        if (vm.isDiscoverySupported) {
            vm.discoveredServers.forEach { server ->
                ServerCard(server, onClick = { onServerSelected(server) }, modifier = Modifier.padding(bottom = 8.dp))
            }
            ProgressRow("Scanning for servers…", Modifier.padding(top = 12.dp, bottom = 24.dp))
        } else {
            Text(
                "Automatic discovery isn't available on this device yet. Please enter the server URL above.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun ServerUrlField(value: String, onValueChange: (String) -> Unit) {
    var showInfo by remember { mutableStateOf(false) }

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
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
                Icon(LibrifinIcons.Info, contentDescription = "About the server URL")
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
