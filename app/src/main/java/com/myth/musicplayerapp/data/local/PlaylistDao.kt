package com.myth.musicplayerapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PlaylistDao {

    @Query(
        """
        SELECT p.id, p.name,
            COUNT(s.id) AS song_count,
            COALESCE(SUM(s.duration_ms), 0) AS total_duration_ms,
            (SELECT s2.content_uri FROM playlist_songs ps2 JOIN songs s2 ON s2.id = ps2.song_id
                WHERE ps2.playlist_id = p.id ORDER BY ps2.position LIMIT 1) AS cover_uri,
            (SELECT s2.album_id FROM playlist_songs ps2 JOIN songs s2 ON s2.id = ps2.song_id
                WHERE ps2.playlist_id = p.id ORDER BY ps2.position LIMIT 1) AS cover_album_id
        FROM playlists p
        LEFT JOIN playlist_songs ps ON ps.playlist_id = p.id
        LEFT JOIN songs s ON s.id = ps.song_id
        GROUP BY p.id
        ORDER BY p.name COLLATE NOCASE ASC
        """
    )
    abstract fun observeSummaries(): Flow<List<PlaylistSummary>>

    @Query(
        "SELECT songs.* FROM songs INNER JOIN playlist_songs ON songs.id = playlist_songs.song_id " +
            "WHERE playlist_songs.playlist_id = :playlistId ORDER BY playlist_songs.position ASC"
    )
    abstract fun observeSongs(playlistId: Long): Flow<List<SongEntity>>

    @Query("SELECT playlist_id FROM playlist_songs WHERE song_id = :songId")
    abstract fun observePlaylistIdsContaining(songId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :playlistId")
    abstract suspend fun rename(playlistId: Long, name: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    abstract suspend fun delete(playlistId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlist_id = :playlistId AND song_id = :songId")
    abstract suspend fun removeSong(playlistId: Long, songId: Long)

    @Query("SELECT MAX(position) FROM playlist_songs WHERE playlist_id = :playlistId")
    protected abstract suspend fun maxPosition(playlistId: Long): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertEntries(entries: List<PlaylistSongCrossRef>)

    /** Appends [songIds] to the end of the playlist, skipping songs already in it. */
    @Transaction
    open suspend fun addSongs(playlistId: Long, songIds: List<Long>) {
        val start = (maxPosition(playlistId) ?: -1) + 1
        insertEntries(songIds.mapIndexed { i, songId -> PlaylistSongCrossRef(playlistId, songId, start + i) })
    }

    @Transaction
    open suspend fun createWithSongs(name: String, createdAt: Long, songIds: List<Long>): Long {
        val id = insertPlaylist(PlaylistEntity(name = name, createdAt = createdAt))
        addSongs(id, songIds)
        return id
    }
}
