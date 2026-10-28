package com.myth.musicplayerapp.data

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.myth.musicplayerapp.data.local.SongEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Reads every music track the device's MediaStore knows about. Call off the main thread. */
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun scan(): List<SongEntity> {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 0"

        val songs = ArrayList<SongEntity>()
        context.contentResolver.query(collection, projection, selection, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                songs += SongEntity(
                    id = id,
                    title = cursor.getString(titleCol)?.takeIf { it.isNotBlank() } ?: "Untitled",
                    artist = cursor.getString(artistCol).orUnknown("Unknown artist"),
                    album = cursor.getString(albumCol).orUnknown("Unknown album"),
                    albumId = cursor.getLong(albumIdCol),
                    durationMs = cursor.getLong(durationCol),
                    contentUri = ContentUris.withAppendedId(collection, id).toString(),
                    dateAdded = cursor.getLong(dateAddedCol),
                )
            }
        }
        return songs
    }

    private fun String?.orUnknown(fallback: String) =
        if (isNullOrBlank() || this == MediaStore.UNKNOWN_STRING) fallback else this
}
