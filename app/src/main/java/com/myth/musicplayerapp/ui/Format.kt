package com.myth.musicplayerapp.ui

/** Formats a duration as `m:ss`, or `h:mm:ss` once it reaches an hour. */
fun formatDuration(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0) / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

/** Formats a total running time for a collection, e.g. `42 min` or `1 hr 5 min`. */
fun formatTotalDuration(ms: Long): String {
    // Rounded to the nearest minute, so two 29-second tracks read "1 min".
    val totalMinutes = (ms.coerceAtLeast(0) + 30_000) / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "$hours hr $minutes min"
        hours > 0 -> "$hours hr"
        totalMinutes > 0 -> "$minutes min"
        else -> "< 1 min"
    }
}

fun songCount(count: Int): String = if (count == 1) "1 song" else "$count songs"
