package io.github.mrtnha.librifin.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Thin outline icons drawn in code, so we don't need an icon library. Tint them via `Icon(tint = …)`. */
object LibrifinIcons {
    val ChevronLeft: ImageVector by lazy {
        outlineIcon("ChevronLeft") {
            moveTo(15f, 6f)
            lineTo(9f, 12f)
            lineTo(15f, 18f)
        }
    }

    /** Circle with an "i". */
    val Info: ImageVector by lazy {
        outlineIcon("Info") {
            // Circle of radius 9 around (12, 12), drawn as two arcs.
            moveTo(3f, 12f)
            arcTo(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = 21f, y1 = 12f)
            arcTo(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = 3f, y1 = 12f)
            moveTo(12f, 11f)
            lineTo(12f, 16.5f)
            moveTo(12f, 7.75f)
            lineTo(12f, 8f)
        }
    }

    /** An open book: two curved pages and a spine. */
    val Book: ImageVector by lazy {
        outlineIcon("Book") {
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
        }
    }

    /** A 24×24 icon made of rounded 1.5-wide strokes. */
    private fun outlineIcon(name: String, pathBuilder: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = pathBuilder,
        ).build()
}
