package com.myth.musicplayerapp.playback

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.myth.musicplayerapp.data.Song

fun Song.toMediaItem(): MediaItem {
    val uri = contentUri.toUri()
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        // The playback URI is dropped when items cross from controller to session;
        // the session restores it from here (see PlaybackService.SessionCallback).
        .setRequestMetadata(MediaItem.RequestMetadata.Builder().setMediaUri(uri).build())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(artworkUri.toUri())
                .setIsPlayable(true)
                .setIsBrowsable(false)
                .build()
        )
        .build()
}

fun MediaItem.withPlayableUri(): MediaItem {
    if (localConfiguration != null) return this
    val uri = requestMetadata.mediaUri ?: return this
    return buildUpon().setUri(uri).build()
}
