package io.github.mrtnha.librifin.ui.licenses

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.entity.Library
import io.github.mrtnha.librifin.ui.components.FlowScaffold

/** The full text of [library]'s licenses, under its name. */
@Composable
fun LicenseScreen(library: Library, onBack: () -> Unit) {
    FlowScaffold(title = library.name, onBack = onBack) {
        library.licenses.forEachIndexed { i, license ->
            if (i > 0) Spacer(Modifier.height(24.dp))
            Text(
                license.licenseContent?.let(::unwrapLines) ?: license.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Many license files break their lines at about 75 characters, which leaves ragged half-empty lines on a phone.
 * Joins such a break when the line before it is long text (so it ran on into the next one) and the next line
 * continues it rather than starting something new (list item, numbered section). Short lines (headings),
 * separator lines and blank lines between paragraphs stay.
 */
private fun unwrapLines(text: String): String = buildString {
    val lines = text.lines().map { it.trimEnd() }
    var joined = false
    lines.forEachIndexed { i, line ->
        append(if (joined) line.trimStart() else line)
        val next = lines.getOrNull(i + 1)?.trimStart() ?: return@buildString
        joined = line.length >= 50 && line.any { it.isLetter() } &&
            next.isNotEmpty() && next[0] !in "-*(" && !next[0].isDigit()
        append(if (joined) ' ' else '\n')
    }
}
