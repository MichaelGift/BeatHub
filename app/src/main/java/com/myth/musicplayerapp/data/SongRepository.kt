package com.myth.musicplayerapp.data

import com.myth.musicplayerapp.data.local.SongDao
import com.myth.musicplayerapp.data.local.SongEntity
import com.myth.musicplayerapp.data.local.toSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val songDao: SongDao,
    private val scanner: MediaStoreScanner,
) {
    fun songs(query: String = ""): Flow<List<Song>> =
        (if (query.isBlank()) songDao.observeAll() else songDao.search(query.trim())).asSongs()

    fun favorites(): Flow<List<Song>> = songDao.observeFavorites().asSongs()

    fun recentlyPlayed(limit: Int = 50): Flow<List<Song>> = songDao.observeRecent(limit).asSongs()

    fun favoriteIds(): Flow<Set<Long>> = songDao.observeFavoriteIds().map { it.toSet() }

    /** Rescans the device and returns how many songs the library now holds. */
    suspend fun refresh(): Int = withContext(Dispatchers.IO) {
        val scanned = scanner.scan()
        songDao.replaceLibrary(scanned)
        scanned.size
    }

    suspend fun setFavorite(id: Long, favorite: Boolean) = songDao.setFavorite(id, favorite)

    suspend fun markPlayed(id: Long) = songDao.markPlayed(id, System.currentTimeMillis())

    private fun Flow<List<SongEntity>>.asSongs() = map { list -> list.map { it.toSong() } }
}
