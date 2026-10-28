package com.myth.musicplayerapp.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myth.musicplayerapp.data.Song
import com.myth.musicplayerapp.data.SongRepository
import com.myth.musicplayerapp.playback.PlaybackConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryFilter(val label: String) {
    All("Songs"),
    Playlists("Playlists"),
    Favorites("Favorites"),
    Recent("Recent"),
}

data class LibraryUiState(
    /** Null until the first database read completes. */
    val songs: List<Song>? = null,
    val query: String = "",
    val filter: LibraryFilter = LibraryFilter.All,
    val isScanning: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: SongRepository,
    private val connection: PlaybackConnection,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(LibraryFilter.All)
    private val isScanning = MutableStateFlow(false)

    private val songs: Flow<List<Song>> = combine(query, filter, ::Pair).flatMapLatest { (q, f) ->
        when (f) {
            LibraryFilter.All -> repository.songs(q)
            // The playlists list comes from PlaylistsViewModel.
            LibraryFilter.Playlists -> flowOf(emptyList())
            LibraryFilter.Favorites -> repository.favorites().matching(q)
            LibraryFilter.Recent -> repository.recentlyPlayed().matching(q)
        }
    }

    val uiState: StateFlow<LibraryUiState> =
        combine(songs, query, filter, isScanning) { songs, q, f, scanning ->
            LibraryUiState(songs = songs, query = q, filter = f, isScanning = scanning)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    init {
        // The library is only shown once storage access is granted, so scanning here is safe.
        refresh()
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onFilterChange(value: LibraryFilter) {
        filter.value = value
    }

    fun refresh() {
        if (isScanning.value) return
        viewModelScope.launch {
            isScanning.value = true
            try {
                repository.refresh()
            } finally {
                isScanning.value = false
            }
        }
    }

    /** Replaces the queue with the visible list and starts at [index]. */
    fun play(index: Int) {
        val visible = uiState.value.songs ?: return
        connection.play(visible, index)
    }

    fun playNext(song: Song) = connection.playNext(song)

    fun addToQueue(song: Song) = connection.addToQueue(song)

    fun toggleFavorite(song: Song) {
        viewModelScope.launch { repository.setFavorite(song.id, !song.isFavorite) }
    }

    private fun Flow<List<Song>>.matching(query: String): Flow<List<Song>> {
        val q = query.trim()
        if (q.isEmpty()) return this
        return map { list ->
            list.filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.artist.contains(q, ignoreCase = true) ||
                    it.album.contains(q, ignoreCase = true)
            }
        }
    }
}
