package com.myth.musicplayerapp.data

import android.os.Build

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val contentUri: String,
    val isFavorite: Boolean = false,
    val lastPlayedAt: Long? = null,
) {
    val artworkUri: String get() = artworkUriFor(contentUri, albumId)
}

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val durationMs: Long,
    val artworkUri: String?,
)

/**
 * Where to load a track's cover art from. From Android 10 the track's own URI is used and
 * resolved through `ContentResolver.loadThumbnail` (see `AudioArtwork`), which reads the
 * embedded picture; older versions use the legacy album-art URI.
 */
fun artworkUriFor(contentUri: String, albumId: Long): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) contentUri
    else "content://media/external/audio/albumart/$albumId"
