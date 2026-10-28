package com.myth.musicplayerapp.ui.queue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.myth.musicplayerapp.playback.PlaybackState
import com.myth.musicplayerapp.ui.components.Artwork
import com.myth.musicplayerapp.ui.components.EmptyState
import com.myth.musicplayerapp.ui.components.PageHeader
import com.myth.musicplayerapp.ui.songCount

@Composable
fun QueueScreen(
    state: PlaybackState,
    onSkipTo: (Int) -> Unit,
    onRemove: (Int) -> Unit,
) {
    if (state.queue.isEmpty()) {
        EmptyState(
            icon = Icons.AutoMirrored.Rounded.QueueMusic,
            title = "Queue is empty",
            message = "Songs you play from the library line up here.",
            modifier = Modifier.statusBarsPadding(),
        )
        return
    }

    val listState = rememberLazyListState()
    LaunchedEffect(state.currentIndex) {
        if (state.currentIndex >= 0) listState.animateScrollToItem((state.currentIndex - 1).coerceAtLeast(0))
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        val remaining = (state.queue.size - state.currentIndex - 1).coerceAtLeast(0)
        PageHeader(title = "Up next", caption = "$remaining of ${songCount(state.queue.size)} left")
        LazyColumn(state = listState, contentPadding = PaddingValues(vertical = 4.dp)) {
            itemsIndexed(state.queue) { index, entry ->
                val isCurrent = index == state.currentIndex
                val played = state.currentIndex >= 0 && index < state.currentIndex
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSkipTo(index) }
                        .alpha(if (played) 0.4f else 1f)
                        .padding(start = 20.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                ) {
                    Artwork(
                        entry.artworkUri,
                        Modifier.size(48.dp),
                        playing = if (isCurrent) state.isPlaying else null,
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            entry.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isCurrent) FontWeight.SemiBold else null,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            entry.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (!isCurrent) {
                        IconButton(onClick = { onRemove(index) }) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Remove ${entry.title} from queue",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    } else {
                        Spacer(Modifier.size(48.dp))
                    }
                }
            }
        }
    }
}
