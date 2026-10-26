package io.github.mrtnha.librifin.ui.reader

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * The reader's sliders: Material 3's upright handle, gaps and end dot, slimmer than its smallest size. In [color],
 * with the rest of the track in the same color dimmed rather than gray. With [steps], a dot marks each stop: in the
 * bars' color on the colored part, in [color] on the dimmed rest. With a [valueLabel], it shows above the handle while
 * the slider is held. The page slider and the text size slider share it, so they look alike.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    valueLabel: String? = null,
) {
    val colors = SliderDefaults.colors(
        thumbColor = color,
        activeTrackColor = color,
        inactiveTrackColor = color.copy(alpha = 0.35f),
        activeTickColor = MaterialTheme.colorScheme.surface,
        inactiveTickColor = color,
    )
    // Shared with the handle, which narrows while the slider is held, and with the label, which shows meanwhile.
    val interactionSource = remember { MutableInteractionSource() }

    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource,
        steps = steps,
        thumb = {
            if (valueLabel == null) {
                SliderDefaults.Thumb(interactionSource, colors = colors, thumbSize = HANDLE_SIZE)
            } else {
                Label(label = { PlainTooltip { Text(valueLabel) } }, interactionSource = interactionSource) {
                    SliderDefaults.Thumb(interactionSource, colors = colors, thumbSize = HANDLE_SIZE)
                }
            }
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                modifier = Modifier.height(10.dp),
                colors = colors,
                thumbTrackGapSize = 5.dp,
            )
        },
        valueRange = valueRange,
    )
}

/** The sliders' handle. Material shortens a slider's track by half its width on each side. */
internal val HANDLE_SIZE = DpSize(4.dp, 20.dp)

/** How long the finger has to rest on a slider before the book follows it. */
internal const val PREVIEW_DELAY_MS = 50L
