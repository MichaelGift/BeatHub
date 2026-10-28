package com.myth.musicplayerapp.ui.playlists

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.myth.musicplayerapp.data.Playlist
import com.myth.musicplayerapp.ui.components.Artwork
import com.myth.musicplayerapp.ui.formatTotalDuration
import com.myth.musicplayerapp.ui.songCount

@Composable
fun PlaylistGrid(
    playlists: List<Playlist>,
    onOpen: (Playlist) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        item(key = "new") {
            Column(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onCreate)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.height(10.dp))
                Text("New playlist", style = MaterialTheme.typography.titleSmall)
                Text(" ", style = MaterialTheme.typography.bodySmall)
            }
        }
        items(playlists, key = { it.id }) { playlist ->
            Column(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpen(playlist) }
            ) {
                Artwork(
                    playlist.artworkUri,
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    cornerRadius = 12.dp,
                )
                Spacer(Modifier.height(10.dp))
                Text(playlist.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (playlist.songCount == 0) "Empty"
                    else "${songCount(playlist.songCount)} · ${formatTotalDuration(playlist.durationMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}
