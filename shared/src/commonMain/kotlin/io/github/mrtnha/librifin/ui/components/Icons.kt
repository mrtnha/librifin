package io.github.mrtnha.librifin.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Icons drawn in code, so we don't need an icon library. Tint them via `Icon(tint = …)`.
 * Most are our own outline drawings; [Logout] is a Material Symbol by Google (Apache-2.0).
 */
object LibrifinIcons {
    /** Standard "back" arrow for the top bar. */
    val ArrowBack: ImageVector by lazy {
        outlineIcon("ArrowBack", strokeWidth = 2f) {
            moveTo(19f, 12f)
            lineTo(5f, 12f)
            moveTo(11f, 6f)
            lineTo(5f, 12f)
            lineTo(11f, 18f)
        }
    }

    /** "logout" from Material Symbols Outlined (weight 400), by Google, Apache-2.0. A filled shape, not a stroke. */
    val Logout: ImageVector by lazy {
        ImageVector.Builder(
            name = "Logout",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(fill = SolidColor(Color.Black)) {
            // Door frame
            moveTo(5f, 21f)
            quadTo(4.18f, 21f, 3.59f, 20.41f)
            reflectiveQuadTo(3f, 19f)
            verticalLineTo(5f)
            quadTo(3f, 4.17f, 3.59f, 3.59f)
            reflectiveQuadTo(5f, 3f)
            horizontalLineToRelative(7f)
            verticalLineTo(5f)
            horizontalLineTo(5f)
            verticalLineTo(19f)
            horizontalLineToRelative(7f)
            verticalLineToRelative(2f)
            horizontalLineTo(5f)
            close()
            // Arrow pointing out
            moveTo(16f, 17f)
            lineTo(14.63f, 15.55f)
            lineTo(17.18f, 13f)
            horizontalLineTo(9f)
            verticalLineTo(11f)
            horizontalLineToRelative(8.18f)
            lineTo(14.63f, 8.45f)
            lineTo(16f, 7f)
            lineToRelative(5f, 5f)
            lineToRelative(-5f, 5f)
            close()
        }.build()
    }

    /** Magnifying glass. */
    val Search: ImageVector by lazy {
        outlineIcon("Search", strokeWidth = 2f) {
            circle(cx = 10.5f, cy = 10.5f, r = 6.5f)
            moveTo(15.5f, 15.5f)
            lineTo(20f, 20f)
        }
    }

    /** Head and shoulders. */
    val Profile: ImageVector by lazy {
        outlineIcon("Profile", strokeWidth = 2f) {
            circle(cx = 12f, cy = 8f, r = 4f)
            moveTo(4.5f, 20f)
            curveTo(4.5f, 16.5f, 7.8f, 14f, 12f, 14f)
            curveTo(16.2f, 14f, 19.5f, 16.5f, 19.5f, 20f)
        }
    }

    /** Circle with an "i". */
    val Info: ImageVector by lazy {
        outlineIcon("Info") {
            circle(cx = 12f, cy = 12f, r = 9f)
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

    /** Full circle, drawn as two half arcs. */
    private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
        moveTo(cx - r, cy)
        arcTo(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = cx + r, y1 = cy)
        arcTo(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = cx - r, y1 = cy)
    }

    /** A 24×24 icon made of rounded strokes. */
    private fun outlineIcon(name: String, strokeWidth: Float = 1.5f, pathBuilder: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = pathBuilder,
        ).build()
}
