package com.myth.musicplayerapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.myth.musicplayerapp.data.Song

/**
 * A track from the device's MediaStore. The primary key is the MediaStore row id, so
 * rescanning the library updates rows in place instead of inserting duplicates.
 */
@Entity(tableName = "songs", indices = [Index("last_played_at"), Index("is_favorite")])
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    @ColumnInfo(name = "album_id") val albumId: Long,
    @ColumnInfo(name = "duration_ms") val durationMs: Long,
    @ColumnInfo(name = "content_uri") val contentUri: String,
    @ColumnInfo(name = "date_added") val dateAdded: Long,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "last_played_at") val lastPlayedAt: Long? = null,
)

fun SongEntity.toSong() = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    albumId = albumId,
    durationMs = durationMs,
    contentUri = contentUri,
    isFavorite = isFavorite,
    lastPlayedAt = lastPlayedAt,
)

/**
 * Merges a fresh MediaStore scan with what is stored: metadata comes from the scan,
 * user state (favorites, play history) is kept from the stored row.
 */
fun mergeScan(existing: List<SongEntity>, scanned: List<SongEntity>): List<SongEntity> {
    val byId = existing.associateBy { it.id }
    return scanned.map { fresh ->
        val old = byId[fresh.id] ?: return@map fresh
        fresh.copy(isFavorite = old.isFavorite, lastPlayedAt = old.lastPlayedAt)
    }
}
