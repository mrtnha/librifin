package io.github.mrtnha.librifin.ui.reader

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.github.mrtnha.librifin.ui.components.LibrifinIcons
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/**
 * The sheet from the "Aa" button: text size, line spacing, page margins, font and theme, one row each.
 * The rows of the same width come first; the fonts and the themes, which scroll sideways to the screen's edges, come
 * last. It covers only the bottom of the screen and doesn't dim the page, so every change shows on the page right
 * away. Swiping it down closes it. Where it doesn't fit, as on a phone held sideways, its rows scroll, and a swipe
 * down from their top closes it. Its selections and outlines are in the reading theme's own color
 * (readerSheetColorScheme).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSheet(
    theme: ReaderTheme,
    onThemeSelected: (ReaderTheme) -> Unit,
    fontSize: Int,
    onFontSizeSelected: (Int) -> Unit,
    lineHeight: Int?,
    onLineHeightSelected: (Int?) -> Unit,
    pageMargins: Int,
    onPageMarginsSelected: (Int) -> Unit,
    font: ReaderFont,
    onFontSelected: (ReaderFont) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var height by remember { mutableIntStateOf(0) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val currentOnClose by rememberUpdatedState(onClose)
    // The rows scroll, and pass on what they don't use of a drag: so a drag anywhere on the sheet moves it too.
    // Also where they have nothing to scroll, as a drag always goes through them first.
    val dragToClose = remember {
        object : NestedScrollConnection {
            // Moved down, the sheet goes back up first when the finger moves up.
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y >= 0f || dragOffset == 0f) return Offset.Zero
                val moved = available.y.coerceAtLeast(-dragOffset)
                dragOffset += moved
                return Offset(0f, moved)
            }

            // What's left of a downward drag once the rows are at their top. Not a fling of the rows that reaches
            // their top: that stops there.
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (available.y <= 0f || source != NestedScrollSource.UserInput) return Offset.Zero
                dragOffset += available.y
                return Offset(0f, available.y)
            }

            // Once the finger lifts, the moved sheet closes or goes back, and the rows don't fling.
            override suspend fun onPreFling(available: Velocity): Velocity {
                if (dragOffset == 0f) return Velocity.Zero
                if (dragOffset > height * CLOSE_DRAG_FRACTION || available.y > CLOSE_VELOCITY) {
                    currentOnClose()
                } else {
                    animate(dragOffset, 0f) { value, _ -> dragOffset = value }
                }
                return available
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { height = it.height }
            .nestedScroll(dragToClose)
            .offset { IntOffset(0, dragOffset.roundToInt()) },
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                // Like the page slider: room for the navigation bar even while it's hidden, so nothing jumps.
                .windowInsetsPadding(
                    systemBarsIgnoringVisibility().only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                )
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BottomSheetDefaults.DragHandle()
            // With the buttons' own 12 dp around their icons, the icons line up with the rows below.
            val sliderPadding = Modifier.padding(horizontal = 12.dp)
            StepSlider(
                steps = READER_FONT_SIZES,
                selected = fontSize,
                onSelected = onFontSizeSelected,
                label = { "$it%" },
                description = "Text size",
                decreaseIcon = LibrifinIcons.TextDecrease,
                decreaseDescription = "Smaller text",
                increaseIcon = LibrifinIcons.TextIncrease,
                increaseDescription = "Larger text",
                modifier = sliderPadding,
            )
            Spacer(Modifier.height(ROW_GAP))
            StepSlider(
                steps = READER_LINE_HEIGHTS,
                selected = lineHeight,
                onSelected = onLineHeightSelected,
                label = ::lineHeightLabel,
                description = "Line spacing",
                decreaseIcon = LibrifinIcons.DensitySmall,
                decreaseDescription = "Less line spacing",
                increaseIcon = LibrifinIcons.DensityMedium,
                increaseDescription = "More line spacing",
                modifier = sliderPadding,
            )
            Spacer(Modifier.height(ROW_GAP))
            StepSlider(
                steps = READER_PAGE_MARGINS,
                selected = pageMargins,
                onSelected = onPageMarginsSelected,
                label = { "$it%" },
                description = "Page margins",
                decreaseIcon = LibrifinIcons.WidthFull,
                decreaseDescription = "Smaller margins",
                increaseIcon = LibrifinIcons.WidthNormal,
                increaseDescription = "Larger margins",
                modifier = sliderPadding,
            )
            Spacer(Modifier.height(ROW_GAP))
            FontChips(font, onFontSelected)
            Spacer(Modifier.height(ROW_GAP))
            ThemeCards(theme, onThemeSelected)
        }
    }
}

/**
 * One card per theme, in its own page and text colors, so the choice looks like the page it gives. Like the
 * fonts, in one row that scrolls sideways to the screen's edges, opened at the selected theme.
 */
@Composable
private fun ThemeCards(selected: ReaderTheme, onSelect: (ReaderTheme) -> Unit) {
    LazyRow(
        modifier = Modifier.selectableGroup(),
        state = rememberLazyListState(initialFirstVisibleItemIndex = selected.ordinal),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(ReaderTheme.entries) { theme ->
            val isSelected = theme == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(THEME_CARD_WIDTH)
                    .height(ROW_HEIGHT)
                    // Also around the others: the black and dark cards would vanish on the dark sheet without it.
                    .framed(
                        fill = theme.background,
                        // The selected card in its own theme's color, not the sheet's: the sheet changes color only
                        // once the page shows the new theme, while a tapped card shows as selected at once.
                        borderColor = if (isSelected) theme.slider else MaterialTheme.colorScheme.outlineVariant,
                        borderWidth = if (isSelected) 2.dp else BORDER_WIDTH,
                    )
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(theme) })
                    .semantics { contentDescription = theme.label },
            ) {
                Text(
                    "Aa",
                    color = theme.text,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.clearAndSetSemantics {},
                )
            }
        }
    }
}

/** About four and a half cards fit across a small phone (360 dp), so the cut-off one shows that the row goes on. */
private val THEME_CARD_WIDTH = 64.dp

private val ReaderTheme.label: String
    get() = when (this) {
        ReaderTheme.DARK -> "Dark"
        ReaderTheme.BLACK -> "Black"
        ReaderTheme.GRAY -> "Gray"
        ReaderTheme.CHARCOAL -> "Charcoal"
        ReaderTheme.LIGHT -> "Light"
        ReaderTheme.PEWTER -> "Pewter"
        ReaderTheme.SAGE -> "Sage"
        ReaderTheme.FOREST -> "Forest"
        ReaderTheme.ROSE -> "Rose"
        ReaderTheme.WINE -> "Wine"
        ReaderTheme.SKY -> "Sky"
        ReaderTheme.MIDNIGHT -> "Midnight"
        ReaderTheme.KRAFT -> "Kraft"
        ReaderTheme.COFFEE -> "Coffee"
        ReaderTheme.LAVENDER -> "Lavender"
        ReaderTheme.PLUM -> "Plum"
    }

/**
 * One of [steps], as a slider with a stop for each, between buttons with [decreaseIcon] and [increaseIcon] for the
 * step before and after. While dragging, the handle and the [label] of its step above it follow the finger, and
 * [onSelected] gets that step whenever the finger rests for a moment, so the page shows it, like the page slider;
 * letting go takes it right away. In the theme's slider color, also like the page slider. Screen readers read it as
 * [description] with the label of the selected step.
 */
@Composable
private fun <T> StepSlider(
    steps: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    label: (T) -> String,
    description: String,
    decreaseIcon: ImageVector,
    decreaseDescription: String,
    increaseIcon: ImageVector,
    increaseDescription: String,
    modifier: Modifier = Modifier,
) {
    // The slider goes through the steps' places in the list, as the steps needn't all be the same size.
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    val selectedIndex = steps.indexOf(selected)
    val index = draggedIndex ?: selectedIndex
    val shownLabel = label(steps[index])

    LaunchedEffect(draggedIndex) {
        val target = draggedIndex ?: return@LaunchedEffect
        delay(PREVIEW_DELAY_MS)
        onSelected(steps[target])
    }

    Row(modifier.fillMaxWidth().height(ROW_HEIGHT), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onSelected(steps[selectedIndex - 1]) }, enabled = selectedIndex > 0) {
            Icon(decreaseIcon, contentDescription = decreaseDescription)
        }
        ReaderSlider(
            value = index.toFloat(),
            onValueChange = { draggedIndex = it.roundToInt() },
            onValueChangeFinished = {
                draggedIndex?.let { onSelected(steps[it]) }
                draggedIndex = null
            },
            valueRange = 0f..steps.lastIndex.toFloat(),
            // The stops between the two ends.
            steps = steps.size - 2,
            // The theme's slider color, from the sheet's colors, so it changes in the same frame as the page.
            color = MaterialTheme.colorScheme.primary,
            valueLabel = shownLabel,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                // Screen readers read the step, not how far along the slider it is.
                .semantics {
                    contentDescription = description
                    stateDescription = shownLabel
                },
        )
        IconButton(onClick = { onSelected(steps[selectedIndex + 1]) }, enabled = selectedIndex < steps.lastIndex) {
            Icon(increaseIcon, contentDescription = increaseDescription)
        }
    }
}

/** All rows are the same height, with the same space between them. */
private val ROW_HEIGHT = 48.dp
private val ROW_GAP = 24.dp

/** The theme cards' corners and border. */
private val ROW_CORNER_RADIUS = 12.dp
private val BORDER_WIDTH = 1.dp

/**
 * An opaque [fill] with a border around it, drawn as a frame: the border color over the whole shape,
 * then the fill inset by [borderWidth], with corners that follow the outer ones. A border drawn on top of
 * the fill instead lets a light fill show through its smoothed outer edge wherever that edge falls between
 * two pixels, as a light seam along some sides but not others.
 */
private fun Modifier.framed(fill: Color, borderColor: Color, borderWidth: Dp): Modifier = this
    .clip(RoundedCornerShape(ROW_CORNER_RADIUS))
    .background(borderColor)
    .padding(borderWidth)
    .clip(RoundedCornerShape(ROW_CORNER_RADIUS - borderWidth))
    .background(fill)

/**
 * The fonts, each name in its own font, in one row that scrolls sideways to the screen's edges.
 * It opens scrolled to the selected font, so that one is always in sight.
 */
@Composable
private fun FontChips(selected: ReaderFont, onSelect: (ReaderFont) -> Unit) {
    LazyRow(
        state = rememberLazyListState(initialFirstVisibleItemIndex = selected.ordinal),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(ReaderFont.entries) { font ->
            FilterChip(
                selected = font == selected,
                onClick = { onSelect(font) },
                label = { Text(font.label, fontFamily = rememberFontFamily(font)) },
                modifier = Modifier.height(ROW_HEIGHT),
            )
        }
    }
}

/** Dragged down further than this part of its height, the sheet closes when let go. */
private const val CLOSE_DRAG_FRACTION = 1f / 3

/** A downward flick faster than this (pixels per second) closes the sheet however short it was. */
private const val CLOSE_VELOCITY = 1500f
