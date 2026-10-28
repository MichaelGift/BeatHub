package com.myth.musicplayerapp.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.dp
import com.myth.musicplayerapp.ui.formatDuration

/**
 * A slim seek bar: a 3dp line whose thumb grows while dragging. Tap or drag to seek; the
 * new position is only committed when the finger lifts.
 */
@Composable
fun SeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val duration by rememberUpdatedState(durationMs.coerceAtLeast(1))
    val seek by rememberUpdatedState(onSeek)
    val fraction = (dragFraction ?: (positionMs.toFloat() / duration)).coerceIn(0f, 1f)
    val thumbRadius by animateDpAsState(if (dragFraction != null) 8.dp else 5.dp, label = "thumb")

    val active = MaterialTheme.colorScheme.onSurface
    val inactive = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)

    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(28.dp)
                .semantics {
                    contentDescription = "Seek"
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                    setProgress { target ->
                        seek((target.coerceIn(0f, 1f) * duration).toLong())
                        true
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset -> seek((offset.x / size.width * duration).toLong()) }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragFraction = (it.x / size.width).coerceIn(0f, 1f) },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            dragFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            dragFraction?.let { seek((it * duration).toLong()) }
                            dragFraction = null
                        },
                        onDragCancel = { dragFraction = null },
                    )
                },
        ) {
            val track = 3.dp.toPx()
            val y = size.height / 2
            val radius = CornerRadius(track / 2)
            drawRoundRect(inactive, Offset(0f, y - track / 2), Size(size.width, track), radius)
            drawRoundRect(active, Offset(0f, y - track / 2), Size(size.width * fraction, track), radius)
            drawCircle(active, thumbRadius.toPx(), Offset(size.width * fraction, y))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val style = MaterialTheme.typography.labelMedium
            val color = MaterialTheme.colorScheme.onSurfaceVariant
            Text(formatDuration((fraction * duration).toLong()), style = style, color = color)
            Text("-" + formatDuration(duration - (fraction * duration).toLong()), style = style, color = color)
        }
    }
}
