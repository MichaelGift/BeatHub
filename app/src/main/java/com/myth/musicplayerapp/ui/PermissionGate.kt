package com.myth.musicplayerapp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.myth.musicplayerapp.ui.components.EmptyState

private val audioPermission =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

/** Shows [content] once the app can read the device's audio, and explains why otherwise. */
@Composable
fun PermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(context.hasAudioPermission()) }
    var askedOnce by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
        askedOnce = true
    }
    // Picks up a grant made from system settings while the app was in the background.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { granted = context.hasAudioPermission() }

    if (granted) {
        content()
    } else if (!askedOnce) {
        EmptyState(
            icon = Icons.Rounded.LibraryMusic,
            title = "Your music, right here",
            message = "BeatHub plays the audio files stored on this device. Allow access so it can find them.",
            actionLabel = "Allow access",
            onAction = { launcher.launch(audioPermission) },
        )
    } else {
        EmptyState(
            icon = Icons.Rounded.LibraryMusic,
            title = "Access needed",
            message = "Without access to audio files there's nothing to play. You can allow it in app settings.",
            actionLabel = "Open settings",
            onAction = { context.openAppSettings() },
        )
    }
}

private fun Context.hasAudioPermission() =
    ContextCompat.checkSelfPermission(this, audioPermission) == PackageManager.PERMISSION_GRANTED

private fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
    )
}
