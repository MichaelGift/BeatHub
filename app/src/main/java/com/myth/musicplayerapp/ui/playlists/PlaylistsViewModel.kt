package com.myth.musicplayerapp.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myth.musicplayerapp.data.Playlist
import com.myth.musicplayerapp.data.PlaylistRepository
import com.myth.musicplayerapp.data.Song
import com.myth.musicplayerapp.data.SongRepository
import com.myth.musicplayerapp.playback.PlaybackConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PlaylistDetailState {
    data object Loading : PlaylistDetailState
    data object Missing : PlaylistDetailState
    data class Loaded(val playlist: Playlist, val songs: List<Song>) : PlaylistDetailState
}

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlists: PlaylistRepository,
    private val songs: SongRepository,
    private val connection: PlaybackConnection,
) : ViewModel() {

    /** Null until the first database read completes. */
    val all: StateFlow<List<Playlist>?> =
        playlists.playlists().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun detail(playlistId: Long): Flow<PlaylistDetailState> =
        combine(playlists.playlist(playlistId), playlists.songs(playlistId)) { playlist, songs ->
            if (playlist == null) PlaylistDetailState.Missing else PlaylistDetailState.Loaded(playlist, songs)
        }

    fun playlistIdsContaining(songId: Long): Flow<Set<Long>> = playlists.playlistIdsContaining(songId)

    /** Creates a playlist, optionally seeded with one song; reports the new id, or null if the name is taken. */
    fun create(name: String, withSongId: Long? = null, onResult: (Long?) -> Unit = {}) {
        viewModelScope.launch { onResult(playlists.create(name, listOfNotNull(withSongId))) }
    }

    fun rename(playlistId: Long, name: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch { onResult(playlists.rename(playlistId, name)) }
    }

    fun delete(playlistId: Long) {
        viewModelScope.launch { playlists.delete(playlistId) }
    }

    fun setMembership(playlistId: Long, songId: Long, member: Boolean) {
        viewModelScope.launch {
            if (member) playlists.addSong(playlistId, songId) else playlists.removeSong(playlistId, songId)
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch { songs.setFavorite(song.id, !song.isFavorite) }
    }

    fun play(songs: List<Song>, startIndex: Int = 0) = connection.play(songs, startIndex, shuffle = false)

    fun shuffle(songs: List<Song>) {
        if (songs.isEmpty()) return
        connection.play(songs, songs.indices.random(), shuffle = true)
    }

    fun playNext(song: Song) = connection.playNext(song)

    fun addToQueue(song: Song) = connection.addToQueue(song)
}
