package com.myth.musicplayerapp.ui.playlists

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myth.musicplayerapp.data.Playlist
import com.myth.musicplayerapp.data.Song
import com.myth.musicplayerapp.ui.components.Artwork
import com.myth.musicplayerapp.ui.components.EmptyState
import com.myth.musicplayerapp.ui.components.NameDialog
import com.myth.musicplayerapp.ui.components.OverflowMenu
import com.myth.musicplayerapp.ui.components.SongMenuItem
import com.myth.musicplayerapp.ui.components.SongRow
import com.myth.musicplayerapp.ui.formatTotalDuration
import com.myth.musicplayerapp.ui.songCount

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    nowPlayingId: String?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    viewModel: PlaylistsViewModel = hiltViewModel(),
) {
    BackHandler(onBack = onBack)
    val state by remember(playlistId) { viewModel.detail(playlistId) }
        .collectAsStateWithLifecycle(initialValue = PlaylistDetailState.Loading)

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        when (val s = state) {
            PlaylistDetailState.Loading -> CircularProgressIndicator(
                Modifier
                    .align(Alignment.Center)
                    .size(28.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // Deleted, possibly from another screen.
            PlaylistDetailState.Missing -> LaunchedEffect(Unit) { onBack() }
            is PlaylistDetailState.Loaded ->
                LoadedPlaylist(s.playlist, s.songs, nowPlayingId, isPlaying, onBack, viewModel)
        }
    }
}

@Composable
private fun LoadedPlaylist(
    playlist: Playlist,
    songs: List<Song>,
    nowPlayingId: String?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    viewModel: PlaylistsViewModel,
) {
    var renaming by remember { mutableStateOf(false) }
    var renameError by remember { mutableStateOf<String?>(null) }
    var confirmingDelete by remember { mutableStateOf(false) }
    var addingToPlaylist by remember { mutableStateOf<Song?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back to playlists")
            }
            Spacer(Modifier.weight(1f))
            OverflowMenu(
                items = listOf(
                    SongMenuItem("Rename", Icons.Rounded.Edit) { renaming = true },
                    SongMenuItem("Delete playlist", Icons.Rounded.Delete) { confirmingDelete = true },
                ),
                contentDescription = "Playlist options",
            )
        }

        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                ) {
                    Artwork(
                        playlist.artworkUri,
                        Modifier
                            .size(196.dp)
                            .shadow(24.dp, RoundedCornerShape(16.dp)),
                        cornerRadius = 16.dp,
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        playlist.name,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (songs.isEmpty()) "Empty"
                        else "${songCount(songs.size)} · ${formatTotalDuration(playlist.durationMs)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (songs.isNotEmpty()) {
                        Spacer(Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { viewModel.play(songs) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                                Text("Play")
                            }
                            FilledTonalButton(onClick = { viewModel.shuffle(songs) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                                Text("Shuffle")
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
            if (songs.isNotEmpty()) {
                itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                    SongRow(
                        song = song,
                        playing = if (song.id.toString() == nowPlayingId) isPlaying else null,
                        onClick = { viewModel.play(songs, index) },
                        menuItems = listOf(
                            SongMenuItem("Play next", Icons.AutoMirrored.Rounded.PlaylistPlay) { viewModel.playNext(song) },
                            SongMenuItem("Add to queue", Icons.AutoMirrored.Rounded.QueueMusic) { viewModel.addToQueue(song) },
                            SongMenuItem("Add to another playlist", Icons.AutoMirrored.Rounded.PlaylistAdd) { addingToPlaylist = song },
                            if (song.isFavorite) {
                                SongMenuItem("Remove from favorites", Icons.Rounded.Favorite) { viewModel.toggleFavorite(song) }
                            } else {
                                SongMenuItem("Add to favorites", Icons.Rounded.FavoriteBorder) { viewModel.toggleFavorite(song) }
                            },
                            SongMenuItem("Remove from playlist", Icons.Rounded.RemoveCircleOutline) {
                                viewModel.setMembership(playlist.id, song.id, member = false)
                            },
                        ),
                        modifier = Modifier.animateItem(),
                    )
                }
            } else {
                item {
                    EmptyState(
                        icon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                        title = "No songs yet",
                        message = "Add songs from the library with a song's ··· menu.",
                        modifier = Modifier.height(260.dp),
                    )
                }
            }
        }
    }

    if (renaming) {
        NameDialog(
            title = "Rename playlist",
            confirmLabel = "Save",
            initialName = playlist.name,
            error = renameError,
            onConfirm = { name ->
                viewModel.rename(playlist.id, name) { ok ->
                    if (ok) {
                        renaming = false
                        renameError = null
                    } else {
                        renameError = "A playlist called \"$name\" already exists"
                    }
                }
            },
            onDismiss = {
                renaming = false
                renameError = null
            },
        )
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("Delete \"${playlist.name}\"?") },
            text = { Text("The playlist is removed. The songs stay in your library.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmingDelete = false
                    viewModel.delete(playlist.id)
                    onBack()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmingDelete = false }) { Text("Cancel") } },
        )
    }

    addingToPlaylist?.let { song ->
        AddToPlaylistDialog(songId = song.id, songTitle = song.title, onDismiss = { addingToPlaylist = null })
    }
}
