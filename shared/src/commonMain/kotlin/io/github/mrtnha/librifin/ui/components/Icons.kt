package io.github.mrtnha.librifin.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Thin outline icons drawn in code, so we don't need an icon library. Tint them via `Icon(tint = …)`. */
object LibrifinIcons {
    /** An open book: two curved pages and a spine. */
    val Book: ImageVector by lazy {
        ImageVector.Builder(
            name = "Book",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            // Left page
            moveTo(12f, 6.5f)
            curveTo(10f, 5f, 6f, 4.6f, 3f, 5.6f)
            lineTo(3f, 18.8f)
            curveTo(6f, 17.8f, 10f, 18.2f, 12f, 19.7f)
            // Right page
            moveTo(12f, 6.5f)
            curveTo(14f, 5f, 18f, 4.6f, 21f, 5.6f)
            lineTo(21f, 18.8f)
            curveTo(18f, 17.8f, 14f, 18.2f, 12f, 19.7f)
            // Spine
            moveTo(12f, 6.5f)
            lineTo(12f, 19.7f)
        }.build()
    }
}
