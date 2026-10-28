package com.myth.musicplayerapp.playback

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSourceBitmapLoader
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import com.myth.musicplayerapp.data.isAudioTrackUri
import com.myth.musicplayerapp.data.loadTrackArtwork
import java.util.concurrent.Executors

/**
 * Supplies cover art to the media notification, lock screen and connected devices. Track
 * URIs are resolved to their embedded art; anything else goes to Media3's default loader.
 */
@OptIn(UnstableApi::class)
class TrackArtworkBitmapLoader(context: Context) : BitmapLoader {

    private val resolver = context.contentResolver
    private val executor: ListeningExecutorService =
        MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor())
    private val fallback = DataSourceBitmapLoader(context)

    override fun supportsMimeType(mimeType: String): Boolean = fallback.supportsMimeType(mimeType)

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> = fallback.decodeBitmap(data)

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && uri.isAudioTrackUri()) {
            executor.submit<Bitmap> { resolver.loadTrackArtwork(uri, ARTWORK_SIZE_PX) }
        } else {
            fallback.loadBitmap(uri)
        }

    fun release() {
        executor.shutdown()
    }

    private companion object {
        const val ARTWORK_SIZE_PX = 512
    }
}
