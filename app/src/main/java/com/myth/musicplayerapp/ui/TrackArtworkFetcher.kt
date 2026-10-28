package com.myth.musicplayerapp.ui

import android.net.Uri
import android.os.Build
import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import coil.size.Dimension
import com.myth.musicplayerapp.data.isAudioTrackUri
import com.myth.musicplayerapp.data.loadTrackArtwork

/** Lets Coil load a track's embedded cover art straight from its MediaStore URI. */
class TrackArtworkFetcher(
    private val uri: Uri,
    private val options: Options,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val width = (options.size.width as? Dimension.Pixels)?.px ?: DEFAULT_SIZE_PX
        val height = (options.size.height as? Dimension.Pixels)?.px ?: DEFAULT_SIZE_PX
        val bitmap = options.context.contentResolver.loadTrackArtwork(uri, maxOf(width, height))
        return DrawableResult(
            drawable = bitmap.toDrawable(options.context.resources),
            isSampled = true,
            dataSource = DataSource.DISK,
        )
    }

    class Factory : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && data.isAudioTrackUri()) {
                TrackArtworkFetcher(data, options)
            } else {
                null
            }
    }

    private companion object {
        const val DEFAULT_SIZE_PX = 512
    }
}
