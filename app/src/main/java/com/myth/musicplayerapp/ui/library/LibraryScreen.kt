package com.myth.musicplayerapp.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myth.musicplayerapp.data.Song
import com.myth.musicplayerapp.ui.components.EmptyState
import com.myth.musicplayerapp.ui.components.NameDialog
import com.myth.musicplayerapp.ui.components.PageHeader
import com.myth.musicplayerapp.ui.components.PillTabs
import com.myth.musicplayerapp.ui.components.SearchField
import com.myth.musicplayerapp.ui.components.SongMenuItem
import com.myth.musicplayerapp.ui.components.SongRow
import com.myth.musicplayerapp.ui.playlists.AddToPlaylistDialog
import com.myth.musicplayerapp.ui.playlists.PlaylistDetailScreen
import com.myth.musicplayerapp.ui.playlists.PlaylistGrid
import com.myth.musicplayerapp.ui.playlists.PlaylistsViewModel
import com.myth.musicplayerapp.ui.songCount

@Composable
fun LibraryScreen(
    nowPlayingId: String?,
    isPlaying: Boolean,
    viewModel: LibraryViewModel = hiltViewModel(),
    playlistsViewModel: PlaylistsViewModel = hiltViewModel(),
) {
    var openPlaylistId by rememberSaveable { mutableStateOf<Long?>(null) }
    openPlaylistId?.let { id ->
        PlaylistDetailScreen(
            playlistId = id,
            nowPlayingId = nowPlayingId,
            isPlaying = isPlaying,
            onBack = { openPlaylistId = null },
        )
        return
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playlists by playlistsViewModel.all.collectAsStateWithLifecycle()
    var addingToPlaylist by remember { mutableStateOf<Song?>(null) }
    var creatingPlaylist by remember { mutableStateOf(false) }
    var createError by remember { mutableStateOf<String?>(null) }

    val showingPlaylists = state.filter == LibraryFilter.Playlists
    val visiblePlaylists = playlists?.filter { it.name.contains(state.query.trim(), ignoreCase = true) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        val caption = if (showingPlaylists) {
            visiblePlaylists?.size?.let { if (it == 1) "1 playlist" else "$it playlists" }
        } else {
            state.songs?.size?.let(::songCount)
        }
        PageHeader(title = "Library", caption = caption) {
            if (state.isScanning) {
                CircularProgressIndicator(
                    Modifier
                        .padding(12.dp)
                        .size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                IconButton(onClick = viewModel::refresh) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Rescan device", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        SearchField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = if (showingPlaylists) "Search playlists" else "Songs, artists, albums",
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(12.dp))
        PillTabs(
            options = LibraryFilter.entries,
            selected = state.filter,
            label = { it.label },
            onSelect = viewModel::onFilterChange,
        )
        Spacer(Modifier.height(8.dp))

        if (showingPlaylists) {
            when {
                visiblePlaylists == null -> Loading()
                visiblePlaylists.isEmpty() && state.query.isNotBlank() -> EmptyState(
                    icon = Icons.Rounded.Search,
                    title = "No matches",
                    message = "No playlist matches \"${state.query}\".",
                )
                else -> PlaylistGrid(
                    playlists = visiblePlaylists,
                    onOpen = { openPlaylistId = it.id },
                    onCreate = { creatingPlaylist = true },
                )
            }
        } else {
            val songs = state.songs
            when {
                songs == null -> Loading()
                songs.isEmpty() -> LibraryEmptyState(state, onRescan = viewModel::refresh)
                else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 4.dp)) {
                    itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                        SongRow(
                            song = song,
                            playing = if (song.id.toString() == nowPlayingId) isPlaying else null,
                            onClick = { viewModel.play(index) },
                            menuItems = listOf(
                                SongMenuItem("Play next", Icons.AutoMirrored.Rounded.PlaylistPlay) { viewModel.playNext(song) },
                                SongMenuItem("Add to queue", Icons.AutoMirrored.Rounded.QueueMusic) { viewModel.addToQueue(song) },
                                SongMenuItem("Add to playlist", Icons.AutoMirrored.Rounded.PlaylistAdd) { addingToPlaylist = song },
                                if (song.isFavorite) {
                                    SongMenuItem("Remove from favorites", Icons.Rounded.Favorite) { viewModel.toggleFavorite(song) }
                                } else {
                                    SongMenuItem("Add to favorites", Icons.Rounded.FavoriteBorder) { viewModel.toggleFavorite(song) }
                                },
                            ),
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

    addingToPlaylist?.let { song ->
        AddToPlaylistDialog(songId = song.id, songTitle = song.title, onDismiss = { addingToPlaylist = null })
    }

    if (creatingPlaylist) {
        NameDialog(
            title = "New playlist",
            confirmLabel = "Create",
            error = createError,
            onConfirm = { name ->
                playlistsViewModel.create(name) { id ->
                    if (id == null) {
                        createError = "A playlist called \"$name\" already exists"
                    } else {
                        creatingPlaylist = false
                        createError = null
                        openPlaylistId = id
                    }
                }
            },
            onDismiss = {
                creatingPlaylist = false
                createError = null
            },
        )
    }
}

@Composable
private fun Loading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LibraryEmptyState(state: LibraryUiState, onRescan: () -> Unit) {
    when {
        state.query.isNotBlank() -> EmptyState(
            icon = Icons.Rounded.Search,
            title = "No matches",
            message = "Nothing matches \"${state.query}\".",
        )
        state.filter == LibraryFilter.Favorites -> EmptyState(
            icon = Icons.Rounded.FavoriteBorder,
            title = "No favorites yet",
            message = "Use a song's menu or the heart on Now playing to keep it here.",
        )
        state.filter == LibraryFilter.Recent -> EmptyState(
            icon = Icons.Rounded.History,
            title = "Nothing played yet",
            message = "Songs you play will show up here.",
        )
        state.isScanning -> Loading()
        else -> EmptyState(
            icon = Icons.Rounded.LibraryMusic,
            title = "No music found",
            message = "Copy audio files to this device, then rescan.",
            actionLabel = "Rescan",
            onAction = onRescan,
        )
    }
}
