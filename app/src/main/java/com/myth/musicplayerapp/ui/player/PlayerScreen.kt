package com.myth.musicplayerapp.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.myth.musicplayerapp.playback.PlaybackState
import com.myth.musicplayerapp.playback.QueueEntry
import com.myth.musicplayerapp.playback.RepeatMode
import com.myth.musicplayerapp.ui.components.Artwork
import com.myth.musicplayerapp.ui.components.EmptyState
import com.myth.musicplayerapp.ui.components.SeekBar
import com.myth.musicplayerapp.ui.playlists.AddToPlaylistDialog

@Composable
fun PlayerScreen(
    state: PlaybackState,
    positionMs: Long,
    isFavorite: Boolean,
    artworkColor: Color,
    viewModel: PlayerViewModel,
    onBrowseLibrary: () -> Unit,
) {
    val current = state.current
    if (current == null) {
        EmptyState(
            icon = Icons.Rounded.GraphicEq,
            title = "Nothing playing",
            message = "Pick a song from your library to start listening.",
            actionLabel = "Browse library",
            onAction = onBrowseLibrary,
            modifier = Modifier.statusBarsPadding(),
        )
        return
    }

    // A wash of the artwork's color that fades into the page background.
    val washAlpha = if (isSystemInDarkTheme()) 0.45f else 0.28f
    val background = MaterialTheme.colorScheme.background
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(0f to artworkColor.copy(alpha = washAlpha), 0.75f to background))
            .statusBarsPadding()
            .padding(horizontal = 28.dp, vertical = 16.dp)
    ) {
        if (maxWidth > maxHeight) {
            // Landscape: artwork beside the controls, since stacking them doesn't fit.
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                PlayerArtwork(current, state.isPlaying, artworkColor, Modifier.fillMaxHeight())
                Spacer(Modifier.width(40.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    NowPlayingControls(state, positionMs, isFavorite, viewModel)
                }
            }
        } else {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.weight(0.25f))
                PlayerArtwork(current, state.isPlaying, artworkColor, Modifier.weight(1f, fill = false))
                Spacer(Modifier.weight(0.2f))
                NowPlayingControls(state, positionMs, isFavorite, viewModel)
                Spacer(Modifier.weight(0.15f))
            }
        }
    }
}

/** Square artwork that eases back when paused, with a soft shadow in its own color. */
@Composable
private fun PlayerArtwork(current: QueueEntry, isPlaying: Boolean, glow: Color, modifier: Modifier) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.86f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
        label = "artworkScale",
    )
    val shape = RoundedCornerShape(20.dp)
    Artwork(
        uri = current.artworkUri,
        cornerRadius = 20.dp,
        modifier = modifier
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = 32.dp, shape = shape, ambientColor = glow, spotColor = glow),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NowPlayingControls(
    state: PlaybackState,
    positionMs: Long,
    isFavorite: Boolean,
    viewModel: PlayerViewModel,
) {
    val current = state.current ?: return
    var addingToPlaylist by remember { mutableStateOf(false) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    current.title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(),
                )
                Text(
                    current.artist,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (current.mediaId.toLongOrNull() != null) {
                IconButton(onClick = { addingToPlaylist = true }) {
                    Icon(
                        Icons.AutoMirrored.Rounded.PlaylistAdd,
                        contentDescription = "Add to playlist",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = viewModel::toggleFavorite) {
                Icon(
                    if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        SeekBar(positionMs = positionMs, durationMs = state.durationMs, onSeek = viewModel::seekTo)
        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            ModeToggle(Icons.Rounded.Shuffle, "Shuffle", active = state.shuffle, onClick = viewModel::toggleShuffle)
            IconButton(onClick = viewModel::previous, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(38.dp))
            }
            PlayPauseButton(isPlaying = state.isPlaying, onClick = viewModel::togglePlayPause)
            IconButton(onClick = viewModel::next, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "Next", modifier = Modifier.size(38.dp))
            }
            ModeToggle(
                icon = if (state.repeat == RepeatMode.One) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                label = "Repeat: ${state.repeat.name.lowercase()}",
                active = state.repeat != RepeatMode.Off,
                onClick = viewModel::cycleRepeat,
            )
        }
    }

    current.mediaId.toLongOrNull()?.let { songId ->
        if (addingToPlaylist) {
            AddToPlaylistDialog(songId = songId, songTitle = current.title, onDismiss = { addingToPlaylist = false })
        }
    }
}

@Composable
private fun PlayPauseButton(isPlaying: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = if (isPlaying) "Pause" else "Play"
            },
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.7f)) togetherWith (fadeOut() + scaleOut(targetScale = 0.7f)) },
            label = "playPause",
        ) { playing ->
            Icon(
                if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(38.dp),
            )
        }
    }
}

/** Shuffle / repeat: dimmed when off, full contrast with a dot underneath when on. */
@Composable
private fun ModeToggle(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.height(3.dp))
            Box(
                Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (active) MaterialTheme.colorScheme.onSurface else Color.Transparent)
            )
        }
    }
}
