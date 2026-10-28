package com.myth.musicplayerapp.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class MergeScanTest {

    private fun song(id: Long, title: String = "Song $id", favorite: Boolean = false, lastPlayed: Long? = null) =
        SongEntity(
            id = id,
            title = title,
            artist = "Artist",
            album = "Album",
            albumId = 1,
            durationMs = 180_000,
            contentUri = "content://media/external/audio/media/$id",
            dateAdded = 0,
            isFavorite = favorite,
            lastPlayedAt = lastPlayed,
        )

    @Test
    fun `keeps favorites and history while taking fresh metadata`() {
        val existing = listOf(song(1, title = "Old title", favorite = true, lastPlayed = 42))
        val scanned = listOf(song(1, title = "New title"))

        val merged = mergeScan(existing, scanned).single()

        assertEquals("New title", merged.title)
        assertEquals(true, merged.isFavorite)
        assertEquals(42L, merged.lastPlayedAt)
    }

    @Test
    fun `new songs pass through and removed songs are dropped`() {
        val existing = listOf(song(1, favorite = true), song(2))
        val scanned = listOf(song(2), song(3))

        val merged = mergeScan(existing, scanned)

        assertEquals(listOf(2L, 3L), merged.map { it.id })
        assertEquals(false, merged.first { it.id == 3L }.isFavorite)
    }
}
