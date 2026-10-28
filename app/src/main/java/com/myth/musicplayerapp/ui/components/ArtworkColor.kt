package com.myth.musicplayerapp.ui.components

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The most characteristic color of the artwork at [uri], animated as the track changes.
 * Falls back to [fallback] when there is no art.
 */
@Composable
fun rememberArtworkColor(uri: Any?, fallback: Color): State<Color> {
    val context = LocalContext.current
    var target by remember { mutableStateOf(fallback) }
    LaunchedEffect(uri, fallback) {
        target = uri?.let { extractColor(context, it) } ?: fallback
    }
    return animateColorAsState(target, tween(durationMillis = 700), label = "artworkColor")
}

private suspend fun extractColor(context: Context, uri: Any): Color? {
    val request = ImageRequest.Builder(context)
        .data(uri)
        .size(96)
        .allowHardware(false)
        .build()
    val drawable = (context.imageLoader.execute(request) as? SuccessResult)?.drawable ?: return null
    return withContext(Dispatchers.Default) {
        var bitmap = drawable.toBitmap()
        // Palette reads pixels, which hardware bitmaps don't allow.
        if (bitmap.config == Bitmap.Config.HARDWARE) bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
        val palette = Palette.from(bitmap).generate()
        (palette.vibrantSwatch ?: palette.dominantSwatch)?.rgb?.let(::Color)
    }
}
