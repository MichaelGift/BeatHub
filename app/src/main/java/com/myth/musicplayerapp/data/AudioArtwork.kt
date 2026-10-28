package com.myth.musicplayerapp.data

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.RequiresApi

/** True for a single track's MediaStore URI, e.g. `content://media/external/audio/media/42`. */
fun Uri.isAudioTrackUri(): Boolean =
    scheme == ContentResolver.SCHEME_CONTENT &&
        authority == MediaStore.AUTHORITY &&
        pathSegments.let { it.size >= 4 && it[it.size - 3] == "audio" && it[it.size - 2] == "media" }

/**
 * Decodes the cover art embedded in a track, scaled to roughly [sizePx].
 * Throws [java.io.IOException] when the track has no art. Blocking; call off the main thread.
 */
@RequiresApi(Build.VERSION_CODES.Q)
fun ContentResolver.loadTrackArtwork(trackUri: Uri, sizePx: Int): Bitmap =
    loadThumbnail(trackUri, Size(sizePx, sizePx), null)
