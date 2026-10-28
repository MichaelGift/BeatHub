package com.myth.musicplayerapp.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SongDao {

    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE ASC")
    abstract fun observeAll(): Flow<List<SongEntity>>

    @Query(
        "SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' " +
            "OR album LIKE '%' || :query || '%' ORDER BY title COLLATE NOCASE ASC"
    )
    abstract fun search(query: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE is_favorite = 1 ORDER BY title COLLATE NOCASE ASC")
    abstract fun observeFavorites(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE last_played_at IS NOT NULL ORDER BY last_played_at DESC LIMIT :limit")
    abstract fun observeRecent(limit: Int): Flow<List<SongEntity>>

    @Query("SELECT id FROM songs WHERE is_favorite = 1")
    abstract fun observeFavoriteIds(): Flow<List<Long>>

    @Query("UPDATE songs SET is_favorite = :favorite WHERE id = :id")
    abstract suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE songs SET last_played_at = :timestamp WHERE id = :id")
    abstract suspend fun markPlayed(id: Long, timestamp: Long)

    @Query("SELECT * FROM songs")
    protected abstract suspend fun getAll(): List<SongEntity>

    @Upsert
    protected abstract suspend fun upsertAll(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    protected abstract suspend fun deleteByIds(ids: List<Long>)

    /** Makes the table mirror [scanned], keeping favorites and history for songs that remain. */
    @Transaction
    open suspend fun replaceLibrary(scanned: List<SongEntity>) {
        val existing = getAll()
        upsertAll(mergeScan(existing, scanned))
        val scannedIds = scanned.mapTo(HashSet()) { it.id }
        // Chunked to stay under SQLite's bound-variable limit on large libraries.
        existing.map { it.id }.filterNot { it in scannedIds }.chunked(500).forEach { deleteByIds(it) }
    }
}
