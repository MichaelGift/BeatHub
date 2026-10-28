package com.myth.musicplayerapp.data

import android.database.sqlite.SQLiteConstraintException
import com.myth.musicplayerapp.data.local.PlaylistDao
import com.myth.musicplayerapp.data.local.toSong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val dao: PlaylistDao,
) {
    fun playlists(): Flow<List<Playlist>> = dao.observeSummaries().map { rows ->
        rows.map { row ->
            Playlist(
                id = row.id,
                name = row.name,
                songCount = row.songCount,
                durationMs = row.totalDurationMs,
                artworkUri = row.coverUri?.let { artworkUriFor(it, row.coverAlbumId ?: 0) },
            )
        }
    }

    fun playlist(id: Long): Flow<Playlist?> = playlists().map { all -> all.firstOrNull { it.id == id } }

    fun songs(playlistId: Long): Flow<List<Song>> =
        dao.observeSongs(playlistId).map { list -> list.map { it.toSong() } }

    fun playlistIdsContaining(songId: Long): Flow<Set<Long>> =
        dao.observePlaylistIdsContaining(songId).map { it.toSet() }

    /** Returns the new playlist's id, or null when the name is already taken. */
    suspend fun create(name: String, songIds: List<Long> = emptyList()): Long? = try {
        dao.createWithSongs(name.trim(), System.currentTimeMillis(), songIds)
    } catch (e: SQLiteConstraintException) {
        null
    }

    /** Returns false when another playlist already has [name]. */
    suspend fun rename(id: Long, name: String): Boolean = try {
        dao.rename(id, name.trim())
        true
    } catch (e: SQLiteConstraintException) {
        false
    }

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun addSong(playlistId: Long, songId: Long) = dao.addSongs(playlistId, listOf(songId))

    suspend fun removeSong(playlistId: Long, songId: Long) = dao.removeSong(playlistId, songId)
}
