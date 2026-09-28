package io.github.mrtnha.librifin.ui.server

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ServerSelectionScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Connect to Jellyfin", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Text("‹ Back")
        }
        Spacer(Modifier.height(16.dp))
        Text("Server discovery and manual entry come next.", style = MaterialTheme.typography.bodyMedium)
    }
}
