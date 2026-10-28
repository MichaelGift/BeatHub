package com.myth.musicplayerapp.ui.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myth.musicplayerapp.ui.components.NameDialog
import com.myth.musicplayerapp.ui.songCount

/** Lets the user tick which playlists contain a song, or start a new one with it. */
@Composable
fun AddToPlaylistDialog(
    songId: Long,
    songTitle: String,
    onDismiss: () -> Unit,
    viewModel: PlaylistsViewModel = hiltViewModel(),
) {
    val playlists by viewModel.all.collectAsStateWithLifecycle()
    val containing by remember(songId) { viewModel.playlistIdsContaining(songId) }
        .collectAsStateWithLifecycle(initialValue = emptySet())
    var creating by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }

    if (creating) {
        NameDialog(
            title = "New playlist",
            confirmLabel = "Create",
            error = nameError,
            onConfirm = { name ->
                viewModel.create(name, withSongId = songId) { id ->
                    if (id == null) nameError = "A playlist called \"$name\" already exists" else onDismiss()
                }
            },
            onDismiss = {
                creating = false
                nameError = null
            },
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to playlist", maxLines = 1) },
        text = {
            LazyColumn(Modifier.heightIn(max = 360.dp)) {
                item {
                    Text(
                        songTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { creating = true }
                            .padding(vertical = 12.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("New playlist", color = MaterialTheme.colorScheme.primary)
                    }
                }
                items(playlists.orEmpty(), key = { it.id }) { playlist ->
                    val member = playlist.id in containing
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setMembership(playlist.id, songId, !member) },
                    ) {
                        Checkbox(
                            checked = member,
                            onCheckedChange = { viewModel.setMembership(playlist.id, songId, it) },
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            playlist.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            songCount(playlist.songCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
}
