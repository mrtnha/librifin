package io.github.mrtnha.librifin.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.mrtnha.librifin.ui.components.LibrifinIcons
import kotlinx.coroutines.launch

/**
 * The sheet from the sort button: one row per order, the current one checked. Choosing one sorts the
 * library and closes the sheet. Swiping it down, tapping beside it or going back closes it unchanged.
 *
 * Material's modal sheet, unlike the reader's appearance sheet: the library always shows the system
 * bars, so the sheet's own window can't bring hidden ones back. It keeps clear of the navigation bar
 * by itself.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSheet(selected: LibrarySort, onSelect: (LibrarySort) -> Unit, onDismiss: () -> Unit) {
    // Opens all the way: three rows need no half-open step.
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val scope = rememberCoroutineScope()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(bottom = 24.dp)) {
            Text(
                "Sort by",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 8.dp),
            )
            Column(Modifier.selectableGroup()) {
                LibrarySort.entries.forEach { sort ->
                    SortRow(
                        label = sort.label,
                        isSelected = sort == selected,
                        onClick = {
                            onSelect(sort)
                            // Slides out first, then closes.
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) onDismiss()
                            }
                        },
                    )
                }
            }
        }
    }
}

private val LibrarySort.label: String
    get() = when (this) {
        LibrarySort.RECENTLY_READ -> "Recently read"
        LibrarySort.AUTHOR -> "Author"
        LibrarySort.TITLE -> "Title"
        LibrarySort.PROGRESS -> "Progress"
        LibrarySort.DATE_ADDED -> "Date added"
    }

/** One order: its name, and a check mark if it's the current one, both in the accent color then. */
@Composable
private fun SortRow(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(ROW_HEIGHT)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 24.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = color, modifier = Modifier.weight(1f))
        if (isSelected) Icon(LibrifinIcons.Check, contentDescription = null, tint = color)
    }
}

/** As high as the rows of the reader's appearance sheet. */
private val ROW_HEIGHT = 48.dp
