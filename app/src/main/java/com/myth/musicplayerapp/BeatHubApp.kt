package com.myth.musicplayerapp

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.myth.musicplayerapp.ui.TrackArtworkFetcher
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BeatHubApp : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(TrackArtworkFetcher.Factory()) }
            .crossfade(true)
            .build()
}
