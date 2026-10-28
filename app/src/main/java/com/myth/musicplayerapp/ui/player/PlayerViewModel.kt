package com.myth.musicplayerapp.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myth.musicplayerapp.data.SongRepository
import com.myth.musicplayerapp.playback.PlaybackConnection
import com.myth.musicplayerapp.playback.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Shared by the player page, the queue page and the mini-player. */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val connection: PlaybackConnection,
    private val repository: SongRepository,
) : ViewModel() {

    val state: StateFlow<PlaybackState> = connection.state

    /** Polled while the UI is visible; stops five seconds after it goes to the background. */
    val positionMs: StateFlow<Long> = flow {
        while (true) {
            emit(connection.positionMs)
            delay(if (state.value.isPlaying) 250 else 500)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val isCurrentFavorite: StateFlow<Boolean> =
        combine(state, repository.favoriteIds()) { playback, favorites ->
            playback.current?.mediaId?.toLongOrNull() in favorites
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun togglePlayPause() = connection.togglePlayPause()
    fun next() = connection.next()
    fun previous() = connection.previous()
    fun seekTo(positionMs: Long) = connection.seekTo(positionMs)
    fun skipTo(index: Int) = connection.skipTo(index)
    fun removeAt(index: Int) = connection.removeAt(index)
    fun toggleShuffle() = connection.toggleShuffle()
    fun cycleRepeat() = connection.cycleRepeat()

    fun toggleFavorite() {
        val id = state.value.current?.mediaId?.toLongOrNull() ?: return
        val favorite = !isCurrentFavorite.value
        viewModelScope.launch { repository.setFavorite(id, favorite) }
    }
}
