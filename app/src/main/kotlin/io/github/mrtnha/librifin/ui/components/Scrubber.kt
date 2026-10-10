package io.github.mrtnha.librifin.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Fast scrolling on the right edge of a grid with [columns] columns: a slim handle that appears while the grid
 * scrolls. It follows the grid, and dragging it scrolls the grid to the same place. Meant for grids whose rows are
 * about equally high, like the library's covers.
 */
@Composable
fun Scrubber(gridState: LazyGridState, columns: Int, modifier: Modifier = Modifier) {
    // Read again whenever the grid scrolls or lays out, so the handle and the drag use the current row height.
    val map by remember(gridState, columns) { derivedStateOf { GridScrollMap.of(gridState, columns) } }
    // Only when there's something to scroll.
    if (!(gridState.canScrollForward || gridState.canScrollBackward)) return

    var dragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    val scrollFraction by remember(gridState, columns) {
        derivedStateOf {
            // At the end the handle is at the end, even if rows of other heights put the estimate a bit short.
            if (!gridState.canScrollForward) {
                1f
            } else {
                map.fraction(gridState.firstVisibleItemIndex / columns, gridState.firstVisibleItemScrollOffset)
            }
        }
    }
    val shown = dragging || gridState.isScrollInProgress
    val alpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = if (shown) tween(150) else tween(durationMillis = 600, delayMillis = 1200),
        label = "scrubber",
    )
    val thumbWidth by animateDpAsState(if (dragging) THUMB_WIDTH_DRAGGING else THUMB_WIDTH, label = "thumb width")
    // While hidden it mustn't catch touches meant for the covers underneath.
    if (alpha == 0f && !shown) return

    val density = LocalDensity.current
    BoxWithConstraints(modifier.fillMaxHeight().width(TOUCH_WIDTH).alpha(alpha)) {
        val thumbPx = with(density) { THUMB_HEIGHT.toPx() }
        val trackPx = (constraints.maxHeight - thumbPx).coerceAtLeast(1f)
        val fraction = if (dragging) dragFraction else scrollFraction
        // Where on the handle the finger holds it, so grabbing it doesn't make it jump.
        var grabPx by remember { mutableFloatStateOf(0f) }

        fun dragTo(y: Float) {
            dragFraction = ((y - grabPx) / trackPx).coerceIn(0f, 1f)
            val (row, rowOffsetPx) = map.rowAt(dragFraction)
            gridState.requestScrollToItem(row * columns, rowOffsetPx)
        }

        Box(
            Modifier
                .matchParentSize()
                .pointerInput(trackPx) {
                    detectVerticalDragGestures(
                        onDragStart = { start ->
                            val thumbTop = scrollFraction * trackPx
                            // On the handle (with some room around it): hold it where it was touched.
                            // Elsewhere on the strip: the handle's middle comes to the finger.
                            grabPx = (start.y - thumbTop).takeIf { it in -GRAB_SLOP.toPx()..thumbPx + GRAB_SLOP.toPx() }
                                ?.coerceIn(0f, thumbPx)
                                ?: (thumbPx / 2)
                            dragging = true
                            dragTo(start.y)
                        },
                        onDragEnd = { dragging = false },
                        onDragCancel = { dragging = false },
                        onVerticalDrag = { change, _ ->
                            change.consume()
                            dragTo(change.position.y)
                        },
                    )
                },
        ) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset { IntOffset(0, (fraction * trackPx).roundToInt()) }
                    .padding(end = 4.dp)
                    .size(width = thumbWidth, height = THUMB_HEIGHT)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)),
            )
        }
    }
}

/**
 * A grid's scroll positions, taken as [rows] rows of [rowPx] each, spacing below included. [rangePx]: how far the
 * grid scrolls from top to bottom.
 */
internal class GridScrollMap(private val rows: Int, private val rowPx: Int, val rangePx: Long) {

    /** Where the grid is, 0 at the top to 1 at the bottom, with [rowOffsetPx] of [firstRow] scrolled away. */
    fun fraction(firstRow: Int, rowOffsetPx: Int): Float =
        if (rangePx <= 0) 0f else ((firstRow.toLong() * rowPx + rowOffsetPx).toFloat() / rangePx).coerceIn(0f, 1f)

    /** The row at [fraction] of the way down, and how far of it is scrolled away. */
    fun rowAt(fraction: Float): Pair<Int, Int> {
        if (rows == 0 || rowPx <= 0) return 0 to 0
        val px = (fraction.coerceIn(0f, 1f) * rangePx).toLong()
        val row = (px / rowPx).toInt()
        return if (row < rows) row to (px % rowPx).toInt() else rows - 1 to 0
    }

    companion object {
        /**
         * For a grid of [rows] rows of [rowPx] each, with [spacingPx] between them, [paddingPx] above and below
         * them all, seen through a [viewportPx] high window.
         */
        fun of(rows: Int, rowPx: Int, spacingPx: Int, paddingPx: Int, viewportPx: Int) = GridScrollMap(
            rows,
            rowPx,
            rangePx = (rows.toLong() * rowPx - spacingPx + paddingPx - viewportPx).coerceAtLeast(0),
        )

        /** For [gridState] as it's laid out now, with its row height measured on the rows in view. */
        fun of(gridState: LazyGridState, columns: Int): GridScrollMap {
            val info = gridState.layoutInfo
            val visible = info.visibleItemsInfo
            val spacingPx = info.mainAxisItemSpacing
            val first = visible.firstOrNull()
            val last = visible.lastOrNull()
            // From the first row in view to the last, so rows of other heights (one or two lines of title)
            // average out. With a single row in view, that row.
            val rowPx = when {
                first == null || last == null -> 0
                last.row > first.row -> (last.offset.y - first.offset.y) / (last.row - first.row)
                else -> first.size.height + spacingPx
            }
            return of(
                rows = (info.totalItemsCount + columns - 1) / columns,
                rowPx = rowPx,
                spacingPx = spacingPx,
                paddingPx = info.beforeContentPadding + info.afterContentPadding,
                viewportPx = info.viewportSize.height,
            )
        }
    }
}

/** The strip that can be grabbed; much wider than the handle it shows. */
private val TOUCH_WIDTH = 32.dp
private val THUMB_HEIGHT = 40.dp
private val THUMB_WIDTH = 4.dp
private val THUMB_WIDTH_DRAGGING = 8.dp
/** How far beside the handle a touch still grabs it rather than jumping. */
private val GRAB_SLOP = 12.dp
