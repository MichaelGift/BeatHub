package com.myth.musicplayerapp.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.myth.musicplayerapp.data.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class QueueEntry(
    val mediaId: String,
    val title: String,
    val artist: String,
    val artworkUri: Uri?,
)

enum class RepeatMode { Off, All, One }

data class PlaybackState(
    val queue: List<QueueEntry> = emptyList(),
    val currentIndex: Int = C.INDEX_UNSET,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0,
    val shuffle: Boolean = false,
    val repeat: RepeatMode = RepeatMode.Off,
) {
    val current: QueueEntry? get() = queue.getOrNull(currentIndex)
}

/**
 * The UI's single handle on playback. Wraps a MediaController connected to
 * [PlaybackService] and mirrors its state as a [StateFlow].
 */
@Singleton
class PlaybackConnection @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var controller: MediaController? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            publish(player, timelineChanged = events.contains(Player.EVENT_TIMELINE_CHANGED))
        }
    }

    private val connecting = scope.async {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        MediaController.Builder(context, token).buildAsync().await().also {
            controller = it
            it.addListener(listener)
            publish(it, timelineChanged = true)
        }
    }

    /** Current position, read on demand since it changes continuously and is not part of [state]. */
    val positionMs: Long get() = controller?.currentPosition ?: 0L

    /** Replaces the queue with [songs]. [shuffle] sets shuffle mode; null keeps the current one. */
    fun play(songs: List<Song>, startIndex: Int, shuffle: Boolean? = null) = withController {
        if (shuffle != null) it.shuffleModeEnabled = shuffle
        it.setMediaItems(songs.map(Song::toMediaItem), startIndex, 0L)
        it.prepare()
        it.play()
    }

    fun playNext(song: Song) = withController {
        if (it.mediaItemCount == 0) {
            it.setMediaItem(song.toMediaItem())
            it.prepare()
        } else {
            it.addMediaItem(it.currentMediaItemIndex + 1, song.toMediaItem())
        }
    }

    fun addToQueue(song: Song) = withController {
        it.addMediaItem(song.toMediaItem())
        if (it.mediaItemCount == 1) it.prepare()
    }

    fun togglePlayPause() = withController {
        when {
            it.isPlaying -> it.pause()
            it.playbackState == Player.STATE_ENDED -> {
                it.seekToDefaultPosition(0)
                it.play()
            }
            else -> {
                if (it.playbackState == Player.STATE_IDLE) it.prepare()
                it.play()
            }
        }
    }

    fun next() = withController { it.seekToNext() }

    fun previous() = withController { it.seekToPrevious() }

    fun seekTo(positionMs: Long) = withController { it.seekTo(positionMs) }

    fun skipTo(index: Int) = withController {
        it.seekToDefaultPosition(index)
        it.play()
    }

    fun removeAt(index: Int) = withController { it.removeMediaItem(index) }

    fun toggleShuffle() = withController { it.shuffleModeEnabled = !it.shuffleModeEnabled }

    fun cycleRepeat() = withController {
        it.repeatMode = when (it.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    private fun withController(block: (MediaController) -> Unit) {
        scope.launch { block(connecting.await()) }
    }

    private fun publish(player: Player, timelineChanged: Boolean) {
        _state.update { old ->
            PlaybackState(
                queue = if (timelineChanged) player.queueEntries() else old.queue,
                currentIndex = if (player.mediaItemCount == 0) C.INDEX_UNSET else player.currentMediaItemIndex,
                isPlaying = player.isPlaying,
                durationMs = player.duration.takeIf { it != C.TIME_UNSET } ?: 0L,
                shuffle = player.shuffleModeEnabled,
                repeat = when (player.repeatMode) {
                    Player.REPEAT_MODE_ALL -> RepeatMode.All
                    Player.REPEAT_MODE_ONE -> RepeatMode.One
                    else -> RepeatMode.Off
                },
            )
        }
    }

    private fun Player.queueEntries() = List(mediaItemCount) { index ->
        val item = getMediaItemAt(index)
        val meta = item.mediaMetadata
        QueueEntry(
            mediaId = item.mediaId,
            title = meta.title?.toString() ?: "Untitled",
            artist = meta.artist?.toString() ?: "Unknown artist",
            artworkUri = meta.artworkUri,
        )
    }
}
