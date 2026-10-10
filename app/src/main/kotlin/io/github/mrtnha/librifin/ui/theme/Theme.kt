package io.github.mrtnha.librifin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Jellyfin's brand colors. The schemes below are Material 3 tonal palettes derived from them. */
val JellyfinBlue = Color(0xFF00A4DC)
val JellyfinPurple = Color(0xFFAA5CC3)

private val LightColors = lightColorScheme(
    primary = Color(0xFF00668A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC4E8FF),
    onPrimaryContainer = Color(0xFF001E2C),
    secondary = Color(0xFF406374),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCE8F8),
    onSecondaryContainer = Color(0xFF1B333F),
    tertiary = Color(0xFF893DA2),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFAD7FF),
    onTertiaryContainer = Color(0xFF330044),
    background = Color(0xFFFCFDFE),
    onBackground = Color(0xFF191C1E),
    surface = Color(0xFFFCFDFE),
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFDDE4E8),
    onSurfaceVariant = Color(0xFF41484D),
    outline = Color(0xFF727A7F),
    outlineVariant = Color(0xFFC0C7CD),
    inversePrimary = Color(0xFF7BD0FF),
)

private val DarkColors = darkColorScheme(
    primary = JellyfinBlue,
    onPrimary = Color(0xFF001E2C),
    primaryContainer = Color(0xFF004C68),
    onPrimaryContainer = Color(0xFFC3E7FF),
    secondary = Color(0xFF60B4DD),
    onSecondary = Color(0xFF112732),
    secondaryContainer = Color(0xFF206B8C),
    onSecondaryContainer = Color(0xFFCEEEFF),
    tertiary = Color(0xFFC979E2),
    onTertiary = Color(0xFF3D0050),
    tertiaryContainer = Color(0xFF762A90),
    onTertiaryContainer = Color(0xFFFAD7FF),
    background = Color(0xFF101315),
    onBackground = Color(0xFFE1E2E5),
    surface = Color(0xFF101315),
    onSurface = Color(0xFFE1E2E5),
    surfaceVariant = Color(0xFF333A3E),
    onSurfaceVariant = Color(0xFFC0C7CD),
    surfaceContainerHighest = Color(0xFF263033),
    surfaceContainerHigh = Color(0xFF1C2427),
    surfaceContainer = Color(0xFF171D20),
    outline = Color(0xFF80878C),
    outlineVariant = Color(0xFF41484D),
    inversePrimary = Color(0xFF00668A),
)

/**
 * Colors for a state that's good ([success], e.g. the server can be reached) or worth noticing ([warning], e.g.
 * it can't). Material's color schemes have no slots for them, so [LibrifinTheme] provides them.
 *
 * A calm green and an amber from Material tonal palettes like the schemes above, their hues turned a little
 * toward Jellyfin's blue so they fit with it. The two differ in brightness as well as in hue, so they can be told
 * apart without seeing red and green: the warning is the brighter one on the dark background and the darker one
 * on the light background, standing out more on both.
 */
@Immutable
data class StatusColors(val success: Color, val warning: Color)

val LocalStatusColors = staticCompositionLocalOf { StatusColors(Color.Unspecified, Color.Unspecified) }

private val LightStatusColors = StatusColors(success = Color(0xFF328659), warning = Color(0xFF865301))
private val DarkStatusColors = StatusColors(success = Color(0xFF67AF84), warning = Color(0xFFFFB863))

@Composable
fun LibrifinTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalStatusColors provides if (darkTheme) DarkStatusColors else LightStatusColors) {
        MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
    }
}

/** The app's colors on a reader page, in the page's [background] and [text] colors. */
fun readerColorScheme(dark: Boolean, background: Color, text: Color): ColorScheme =
    (if (dark) DarkColors else LightColors).copy(
        background = background,
        onBackground = text,
        surface = background,
        onSurface = text,
    )

/** The app's dark colors on the reader's bars, in their [background] color. */
fun readerBarsColorScheme(background: Color): ColorScheme =
    DarkColors.copy(background = background, surface = background)
