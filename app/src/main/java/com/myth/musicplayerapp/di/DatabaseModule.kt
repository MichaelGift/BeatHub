package com.myth.musicplayerapp.di

import android.content.Context
import androidx.room.Room
import com.myth.musicplayerapp.data.local.MusicDatabase
import com.myth.musicplayerapp.data.local.PlaylistDao
import com.myth.musicplayerapp.data.local.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MusicDatabase =
        Room.databaseBuilder(context, MusicDatabase::class.java, "beathub.db").build()

    @Provides
    fun provideSongDao(db: MusicDatabase): SongDao = db.songDao()

    @Provides
    fun providePlaylistDao(db: MusicDatabase): PlaylistDao = db.playlistDao()
}
