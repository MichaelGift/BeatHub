package com.myth.musicplayerapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.myth.musicplayerapp.data.Song
import com.myth.musicplayerapp.ui.formatDuration

data class SongMenuItem(val label: String, val icon: ImageVector, val onClick: () -> Unit)

/**
 * A song in a list. [playing] is null unless this is the current track, in which case the
 * artwork shows the now-playing bars.
 */
@Composable
fun SongRow(
    song: Song,
    playing: Boolean?,
    onClick: () -> Unit,
    menuItems: List<SongMenuItem>,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 20.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
    ) {
        Artwork(song.artworkUri, Modifier.size(48.dp), playing = playing)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (playing != null) FontWeight.SemiBold else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (song.isFavorite) {
                    Icon(
                        Icons.Rounded.Favorite,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(12.dp),
                    )
                }
                Text(
                    "${song.artist} · ${formatDuration(song.durationMs)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        OverflowMenu(menuItems, contentDescription = "More options for ${song.title}")
    }
}

@Composable
fun OverflowMenu(items: List<SongMenuItem>, contentDescription: String) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Rounded.MoreHoriz,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.label, style = MaterialTheme.typography.bodyLarge) },
                    leadingIcon = { Icon(item.icon, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    onClick = {
                        expanded = false
                        item.onClick()
                    },
                )
            }
        }
    }
}
