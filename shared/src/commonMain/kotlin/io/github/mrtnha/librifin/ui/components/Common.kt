package io.github.mrtnha.librifin.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.mrtnha.librifin.api.Server

/** Small logo, heading and a back button, shared by the connect and login screens. */
@Composable
fun FlowHeader(title: String, backLabel: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = LibrifinIcons.Book,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 32.dp, bottom = 20.dp).size(75.dp),
        )
        Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start).padding(top = 20.dp, bottom = 12.dp)) {
            Icon(LibrifinIcons.ChevronLeft, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(backLabel)
        }
    }
}

/** A server the user can pick: name, version and address. */
@Composable
fun ServerCard(server: Server, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                server.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            server.version?.let { Text("v$it", style = MaterialTheme.typography.bodySmall) }
            Text(
                server.baseUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Small spinner with a text next to it, e.g. "Connecting to server…". */
@Composable
fun ProgressRow(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        Text(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 12.dp))
    }
}
