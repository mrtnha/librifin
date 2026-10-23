package io.github.mrtnha.librifin.ui.reader

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mrtnha.librifin.ui.components.LibrifinIcons
import kotlin.math.roundToInt

/**
 * The sheet from the "Aa" button: text size, alignment, font and theme, one row each. The rows of the same
 * width come first; the fonts and the themes, which scroll sideways to the screen's edges, come last. It covers
 * only the bottom of the screen and doesn't dim the page, so every change shows on the page right away.
 * Swiping it down closes it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSheet(
    theme: ReaderTheme,
    onThemeSelected: (ReaderTheme) -> Unit,
    canDecreaseFontSize: Boolean,
    canIncreaseFontSize: Boolean,
    onDecreaseFontSize: () -> Unit,
    onIncreaseFontSize: () -> Unit,
    alignment: ReaderAlignment,
    onAlignmentSelected: (ReaderAlignment) -> Unit,
    font: ReaderFont,
    onFontSelected: (ReaderFont) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var height by remember { mutableIntStateOf(0) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { height = it.height }
            // Before the offset, so the finger's movement is measured where the sheet rests, not where it's dragged to.
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta -> dragOffset = (dragOffset + delta).coerceAtLeast(0f) },
                onDragStopped = { velocity ->
                    if (dragOffset > height * CLOSE_DRAG_FRACTION || velocity > CLOSE_VELOCITY) {
                        onClose()
                    } else {
                        animate(dragOffset, 0f) { value, _ -> dragOffset = value }
                    }
                },
            )
            .offset { IntOffset(0, dragOffset.roundToInt()) },
    ) {
        Column(
            Modifier
                // Like the page slider: room for the navigation bar even while it's hidden, so nothing jumps.
                .windowInsetsPadding(
                    systemBarsIgnoringVisibility().only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                )
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BottomSheetDefaults.DragHandle()
            FontSizeControl(
                canDecrease = canDecreaseFontSize,
                canIncrease = canIncreaseFontSize,
                onDecrease = onDecreaseFontSize,
                onIncrease = onIncreaseFontSize,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Spacer(Modifier.height(ROW_GAP))
            AlignmentControl(alignment, onAlignmentSelected, Modifier.padding(horizontal = 24.dp))
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
            val colors = MaterialTheme.colorScheme
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(THEME_CARD_WIDTH)
                    .height(ROW_HEIGHT)
                    // Also around the others: the black and dark cards would vanish on the dark sheet without it.
                    .framed(
                        fill = theme.background,
                        borderColor = if (isSelected) colors.primary else colors.outlineVariant,
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
 * Smaller and larger text as one control split in two halves: a small and a large "A" show what each
 * half does, no symbols to decode. Tinted with the text color, so it stands out on the sheet in every theme.
 */
@Composable
private fun FontSizeControl(
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val onSurface = colors.onSurface
    Row(
        modifier
            .fillMaxWidth()
            .height(ROW_HEIGHT)
            .framed(
                // Made opaque over the sheet, so the border color doesn't show through the tint.
                fill = onSurface.copy(alpha = CONTROL_TINT_ALPHA).compositeOver(colors.surface),
                borderColor = colors.outlineVariant,
                borderWidth = BORDER_WIDTH,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FontSizeHalf(SMALL_LETTER_SIZE, "Smaller text", canDecrease, onDecrease)
        VerticalDivider(Modifier.height(24.dp), color = onSurface.copy(alpha = DIVIDER_ALPHA))
        FontSizeHalf(LARGE_LETTER_SIZE, "Larger text", canIncrease, onIncrease)
    }
}

/** One half of the text size control. Screen readers read [description] instead of the letter. */
@Composable
private fun RowScope.FontSizeHalf(letterSize: TextUnit, description: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
    ) {
        Text(
            "A",
            fontSize = letterSize,
            lineHeight = letterSize,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else DISABLED_ALPHA),
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

/** All rows are the same height, with the same space between them. */
private val ROW_HEIGHT = 48.dp
private val ROW_GAP = 24.dp

/**
 * The theme cards, the text size control and the alignment control share their corners and border, so they
 * look like one family.
 */
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

private val SMALL_LETTER_SIZE = 14.sp
private val LARGE_LETTER_SIZE = 24.sp

/** How strongly the text color tints the text size control, and its divider. */
private const val CONTROL_TINT_ALPHA = 0.12f
private const val DIVIDER_ALPHA = 0.24f

/** Material's opacity for disabled content: at the smallest or largest size, that half looks inactive. */
private const val DISABLED_ALPHA = 0.38f

/**
 * Left-aligned or justified text, as one control split in two halves with an icon each. Like the font chips,
 * the color alone shows the choice.
 */
@Composable
private fun AlignmentControl(
    selected: ReaderAlignment,
    onSelect: (ReaderAlignment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
        ReaderAlignment.entries.forEachIndexed { index, alignment ->
            SegmentedButton(
                selected = alignment == selected,
                onClick = { onSelect(alignment) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = ReaderAlignment.entries.size,
                    baseShape = RoundedCornerShape(ROW_CORNER_RADIUS),
                ),
                modifier = Modifier.height(ROW_HEIGHT),
                colors = SegmentedButtonDefaults.colors(
                    activeBorderColor = borderColor,
                    inactiveBorderColor = borderColor,
                ),
                // No checkmark, which Material shows here by default.
                icon = {},
                label = { Icon(alignment.icon, contentDescription = alignment.label) },
            )
        }
    }
}

private val ReaderAlignment.icon: ImageVector
    get() = when (this) {
        ReaderAlignment.LEFT -> LibrifinIcons.FormatAlignLeft
        ReaderAlignment.JUSTIFIED -> LibrifinIcons.FormatAlignJustify
    }

private val ReaderAlignment.label: String
    get() = when (this) {
        ReaderAlignment.LEFT -> "Left-aligned"
        ReaderAlignment.JUSTIFIED -> "Justified"
    }

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
