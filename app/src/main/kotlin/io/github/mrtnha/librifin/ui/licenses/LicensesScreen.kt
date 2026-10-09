package io.github.mrtnha.librifin.ui.licenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mikepenz.aboutlibraries.entity.Library
import io.github.mrtnha.librifin.AppServices
import io.github.mrtnha.librifin.ui.components.LibrifinIcons

/**
 * Every library, font and icon set bundled into the app, sorted by name, with its license. Tapping one opens
 * the full license text, which the licenses require to come with the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(services: AppServices, onBack: () -> Unit, onLibraryClick: (Library) -> Unit) {
    val vm = viewModel { LicensesViewModel(services::readLicensesJson) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Source Licenses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(LibrifinIcons.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        // While loading (briefly), nothing is shown
        val libraries = vm.libraries ?: return@Scaffold
        if (libraries.isEmpty()) {
            Text(
                "Not available on this device yet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(innerPadding).padding(horizontal = 24.dp, vertical = 16.dp),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding), state = vm.listState) {
                items(libraries, key = { it.uniqueId }) { library ->
                    ListItem(
                        headlineContent = { Text(library.name) },
                        supportingContent = { Text(library.licenses.joinToString { it.name }) },
                        modifier = Modifier.clickable { onLibraryClick(library) },
                    )
                }
            }
        }
    }
}
