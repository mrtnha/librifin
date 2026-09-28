package io.github.mrtnha.librifin.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * The app's icons: Material Symbols Outlined (weight 400) by Google, Apache-2.0, see README.
 * Their vector paths are embedded here, so there's no icon library and no network access.
 * New ones: download the ".kt" export from https://fonts.google.com/icons and copy its path commands.
 * Tint them via `Icon(tint = …)`.
 */
object LibrifinIcons {
    /** "arrow_back". Top bar back button. */
    val ArrowBack: ImageVector by lazy {
        symbol("ArrowBack") {
            moveTo(7.83f, 13f)
            lineToRelative(5.6f, 5.6f)
            lineTo(12f, 20f)
            lineTo(4f, 12f)
            lineTo(12f, 4f)
            lineToRelative(1.43f, 1.4f)
            lineTo(7.83f, 11f)
            horizontalLineTo(20f)
            verticalLineToRelative(2f)
            horizontalLineTo(7.83f)
            close()
        }
    }

    /** "import_contacts" (an open book). App logo placeholder and cover placeholder. */
    val Book: ImageVector by lazy {
        symbol("Book") {
            moveTo(6.5f, 16f)
            quadToRelative(1.18f, 0f, 2.29f, 0.26f)
            reflectiveQuadTo(11f, 17.05f)
            verticalLineTo(7.2f)
            quadTo(9.98f, 6.6f, 8.83f, 6.3f)
            reflectiveQuadTo(6.5f, 6f)
            quadTo(5.6f, 6f, 4.71f, 6.18f)
            reflectiveQuadTo(3f, 6.7f)
            verticalLineToRelative(9.9f)
            quadTo(3.88f, 16.3f, 4.74f, 16.15f)
            reflectiveQuadTo(6.5f, 16f)
            close()
            moveTo(13f, 17.05f)
            quadToRelative(1.1f, -0.53f, 2.21f, -0.79f)
            reflectiveQuadTo(17.5f, 16f)
            quadToRelative(0.9f, 0f, 1.76f, 0.15f)
            reflectiveQuadTo(21f, 16.6f)
            verticalLineTo(6.7f)
            quadTo(20.18f, 6.35f, 19.29f, 6.18f)
            reflectiveQuadTo(17.5f, 6f)
            quadTo(16.33f, 6f, 15.18f, 6.3f)
            reflectiveQuadTo(13f, 7.2f)
            verticalLineToRelative(9.85f)
            close()
            moveTo(12f, 20f)
            quadTo(10.8f, 19.05f, 9.4f, 18.52f)
            reflectiveQuadTo(6.5f, 18f)
            quadTo(5.45f, 18f, 4.44f, 18.27f)
            reflectiveQuadTo(2.5f, 19.05f)
            quadTo(1.98f, 19.33f, 1.49f, 19.02f)
            quadTo(1f, 18.73f, 1f, 18.15f)
            verticalLineTo(6.1f)
            quadTo(1f, 5.82f, 1.14f, 5.57f)
            quadTo(1.28f, 5.32f, 1.55f, 5.2f)
            quadTo(2.7f, 4.6f, 3.95f, 4.3f)
            reflectiveQuadTo(6.5f, 4f)
            quadTo(7.95f, 4f, 9.34f, 4.38f)
            reflectiveQuadTo(12f, 5.5f)
            quadTo(13.28f, 4.75f, 14.66f, 4.38f)
            reflectiveQuadTo(17.5f, 4f)
            quadToRelative(1.3f, 0f, 2.55f, 0.3f)
            reflectiveQuadToRelative(2.4f, 0.9f)
            quadToRelative(0.27f, 0.13f, 0.41f, 0.38f)
            reflectiveQuadTo(23f, 6.1f)
            verticalLineTo(18.15f)
            quadToRelative(0f, 0.58f, -0.49f, 0.88f)
            quadToRelative(-0.49f, 0.3f, -1.01f, 0.03f)
            quadToRelative(-0.92f, -0.5f, -1.94f, -0.78f)
            reflectiveQuadTo(17.5f, 18f)
            quadTo(16f, 18f, 14.6f, 18.52f)
            reflectiveQuadTo(12f, 20f)
            close()
            moveTo(7f, 11.65f)
            close()
        }
    }

    /** "info". Help next to the server address field. */
    val Info: ImageVector by lazy {
        symbol("Info") {
            moveTo(11f, 17f)
            horizontalLineToRelative(2f)
            verticalLineTo(11f)
            horizontalLineTo(11f)
            verticalLineToRelative(6f)
            close()
            moveTo(12.71f, 8.71f)
            quadTo(13f, 8.42f, 13f, 8f)
            quadTo(13f, 7.57f, 12.71f, 7.29f)
            reflectiveQuadTo(12f, 7f)
            reflectiveQuadTo(11.29f, 7.29f)
            reflectiveQuadTo(11f, 8f)
            quadToRelative(0f, 0.42f, 0.29f, 0.71f)
            reflectiveQuadTo(12f, 9f)
            reflectiveQuadTo(12.71f, 8.71f)
            close()
            moveTo(12f, 22f)
            quadTo(9.93f, 22f, 8.1f, 21.21f)
            quadTo(6.28f, 20.43f, 4.93f, 19.08f)
            quadTo(3.58f, 17.73f, 2.79f, 15.9f)
            reflectiveQuadTo(2f, 12f)
            quadTo(2f, 9.92f, 2.79f, 8.1f)
            quadTo(3.58f, 6.27f, 4.93f, 4.93f)
            quadTo(6.28f, 3.57f, 8.1f, 2.79f)
            quadTo(9.93f, 2f, 12f, 2f)
            reflectiveQuadToRelative(3.9f, 0.79f)
            reflectiveQuadToRelative(3.17f, 2.14f)
            quadToRelative(1.35f, 1.35f, 2.14f, 3.17f)
            quadTo(22f, 9.92f, 22f, 12f)
            reflectiveQuadToRelative(-0.79f, 3.9f)
            reflectiveQuadToRelative(-2.14f, 3.17f)
            quadToRelative(-1.35f, 1.35f, -3.17f, 2.14f)
            reflectiveQuadTo(12f, 22f)
            close()
            moveToRelative(0f, -2f)
            quadToRelative(3.35f, 0f, 5.68f, -2.32f)
            reflectiveQuadTo(20f, 12f)
            reflectiveQuadTo(17.68f, 6.32f)
            reflectiveQuadTo(12f, 4f)
            reflectiveQuadTo(6.33f, 6.32f)
            reflectiveQuadTo(4f, 12f)
            reflectiveQuadToRelative(2.33f, 5.68f)
            reflectiveQuadTo(12f, 20f)
            close()
            moveToRelative(0f, -8f)
            close()
        }
    }

    /** "login". Log in button. */
    val Login: ImageVector by lazy {
        symbol("Login") {
            moveTo(12f, 21f)
            verticalLineTo(19f)
            horizontalLineToRelative(7f)
            verticalLineTo(5f)
            horizontalLineTo(12f)
            verticalLineTo(3f)
            horizontalLineToRelative(7f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(21f, 5f)
            verticalLineTo(19f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(19f, 21f)
            horizontalLineTo(12f)
            close()
            moveTo(10f, 17f)
            lineTo(8.63f, 15.55f)
            lineTo(11.18f, 13f)
            horizontalLineTo(3f)
            verticalLineTo(11f)
            horizontalLineToRelative(8.18f)
            lineTo(8.63f, 8.45f)
            lineTo(10f, 7f)
            lineToRelative(5f, 5f)
            lineToRelative(-5f, 5f)
            close()
        }
    }

    /** "logout". Log out button. */
    val Logout: ImageVector by lazy {
        symbol("Logout") {
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
        }
    }

    /** "account_circle". Profile button in the library top bar. */
    val Profile: ImageVector by lazy {
        symbol("Profile") {
            moveTo(5.85f, 17.1f)
            quadTo(7.13f, 16.13f, 8.7f, 15.56f)
            reflectiveQuadTo(12f, 15f)
            reflectiveQuadToRelative(3.3f, 0.56f)
            reflectiveQuadToRelative(2.85f, 1.54f)
            quadToRelative(0.88f, -1.03f, 1.36f, -2.33f)
            reflectiveQuadTo(20f, 12f)
            quadTo(20f, 8.67f, 17.66f, 6.34f)
            reflectiveQuadTo(12f, 4f)
            quadTo(8.68f, 4f, 6.34f, 6.34f)
            reflectiveQuadTo(4f, 12f)
            quadToRelative(0f, 1.47f, 0.49f, 2.78f)
            quadToRelative(0.49f, 1.3f, 1.36f, 2.33f)
            close()
            moveTo(9.51f, 11.99f)
            quadTo(8.5f, 10.98f, 8.5f, 9.5f)
            quadTo(8.5f, 8.02f, 9.51f, 7.01f)
            reflectiveQuadTo(12f, 6f)
            reflectiveQuadToRelative(2.49f, 1.01f)
            reflectiveQuadTo(15.5f, 9.5f)
            reflectiveQuadToRelative(-1.01f, 2.49f)
            reflectiveQuadTo(12f, 13f)
            quadTo(10.53f, 13f, 9.51f, 11.99f)
            close()
            moveTo(12f, 22f)
            quadTo(9.93f, 22f, 8.1f, 21.21f)
            quadTo(6.28f, 20.43f, 4.93f, 19.08f)
            quadTo(3.58f, 17.73f, 2.79f, 15.9f)
            reflectiveQuadTo(2f, 12f)
            quadTo(2f, 9.92f, 2.79f, 8.1f)
            quadTo(3.58f, 6.27f, 4.93f, 4.93f)
            quadTo(6.28f, 3.57f, 8.1f, 2.79f)
            quadTo(9.93f, 2f, 12f, 2f)
            reflectiveQuadToRelative(3.9f, 0.79f)
            reflectiveQuadToRelative(3.17f, 2.14f)
            quadToRelative(1.35f, 1.35f, 2.14f, 3.17f)
            quadTo(22f, 9.92f, 22f, 12f)
            reflectiveQuadToRelative(-0.79f, 3.9f)
            reflectiveQuadToRelative(-2.14f, 3.17f)
            quadToRelative(-1.35f, 1.35f, -3.17f, 2.14f)
            reflectiveQuadTo(12f, 22f)
            close()
            moveToRelative(2.5f, -2.39f)
            quadToRelative(1.18f, -0.39f, 2.15f, -1.11f)
            quadTo(15.68f, 17.77f, 14.5f, 17.39f)
            reflectiveQuadTo(12f, 17f)
            reflectiveQuadTo(9.5f, 17.39f)
            quadTo(8.33f, 17.77f, 7.35f, 18.5f)
            quadToRelative(0.98f, 0.73f, 2.15f, 1.11f)
            reflectiveQuadTo(12f, 20f)
            reflectiveQuadToRelative(2.5f, -0.39f)
            close()
            moveTo(13.08f, 10.58f)
            quadTo(13.5f, 10.15f, 13.5f, 9.5f)
            reflectiveQuadTo(13.08f, 8.42f)
            reflectiveQuadTo(12f, 8f)
            reflectiveQuadTo(10.93f, 8.42f)
            reflectiveQuadTo(10.5f, 9.5f)
            reflectiveQuadToRelative(0.43f, 1.07f)
            reflectiveQuadTo(12f, 11f)
            reflectiveQuadToRelative(1.08f, -0.43f)
            close()
            moveTo(12f, 9.5f)
            close()
            moveToRelative(0f, 9f)
            close()
        }
    }

    /** "search". Search button in the library top bar. */
    val Search: ImageVector by lazy {
        symbol("Search") {
            moveTo(19.6f, 21f)
            lineTo(13.3f, 14.7f)
            quadToRelative(-0.75f, 0.6f, -1.72f, 0.95f)
            reflectiveQuadTo(9.5f, 16f)
            quadTo(6.78f, 16f, 4.89f, 14.11f)
            quadTo(3f, 12.23f, 3f, 9.5f)
            quadTo(3f, 6.77f, 4.89f, 4.89f)
            reflectiveQuadTo(9.5f, 3f)
            reflectiveQuadToRelative(4.61f, 1.89f)
            reflectiveQuadTo(16f, 9.5f)
            quadToRelative(0f, 1.1f, -0.35f, 2.07f)
            reflectiveQuadTo(14.7f, 13.3f)
            lineTo(21f, 19.6f)
            lineTo(19.6f, 21f)
            close()
            moveTo(9.5f, 14f)
            quadToRelative(1.88f, 0f, 3.19f, -1.31f)
            reflectiveQuadTo(14f, 9.5f)
            reflectiveQuadTo(12.69f, 6.31f)
            reflectiveQuadTo(9.5f, 5f)
            reflectiveQuadTo(6.31f, 6.31f)
            reflectiveQuadTo(5f, 9.5f)
            reflectiveQuadToRelative(1.31f, 3.19f)
            reflectiveQuadTo(9.5f, 14f)
            close()
        }
    }

    /** A 24×24 icon made of one filled path. */
    private fun symbol(name: String, pathBuilder: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(fill = SolidColor(Color.Black), pathBuilder = pathBuilder).build()
}
