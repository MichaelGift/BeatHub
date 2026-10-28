package com.myth.musicplayerapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myth.musicplayerapp.ui.components.MiniPlayer
import com.myth.musicplayerapp.ui.components.rememberArtworkColor
import com.myth.musicplayerapp.ui.library.LibraryScreen
import com.myth.musicplayerapp.ui.player.PlayerScreen
import com.myth.musicplayerapp.ui.player.PlayerViewModel
import com.myth.musicplayerapp.ui.queue.QueueScreen
import kotlinx.coroutines.launch

/** The three swipeable pages, with the player in the middle. */
private enum class Page(val label: String, val icon: ImageVector) {
    Queue("Queue", Icons.AutoMirrored.Rounded.QueueMusic),
    Player("Playing", Icons.Rounded.GraphicEq),
    Library("Library", Icons.Rounded.LibraryMusic),
}

@Composable
fun AppRoot() {
    PermissionGate {
        val playerViewModel: PlayerViewModel = hiltViewModel()
        val playback by playerViewModel.state.collectAsStateWithLifecycle()
        val positionMs by playerViewModel.positionMs.collectAsStateWithLifecycle()
        val isFavorite by playerViewModel.isCurrentFavorite.collectAsStateWithLifecycle()
        val artworkColor by rememberArtworkColor(playback.current?.artworkUri, MaterialTheme.colorScheme.onSurface)

        val pagerState = rememberPagerState(initialPage = Page.Library.ordinal) { Page.entries.size }
        val scope = rememberCoroutineScope()
        fun goTo(page: Page) = scope.launch { pagerState.animateScrollToPage(page.ordinal) }

        Scaffold(
            // Each page handles the status bar itself so the player can draw behind it.
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                    val current = playback.current
                    AnimatedVisibility(
                        visible = current != null && pagerState.currentPage != Page.Player.ordinal,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        if (current != null) {
                            MiniPlayer(
                                entry = current,
                                isPlaying = playback.isPlaying,
                                progress = if (playback.durationMs > 0) positionMs.toFloat() / playback.durationMs else 0f,
                                accent = artworkColor,
                                onClick = { goTo(Page.Player) },
                                onPlayPause = playerViewModel::togglePlayPause,
                                onNext = playerViewModel::next,
                            )
                        }
                    }
                    BottomBar(selected = Page.entries[pagerState.currentPage], onSelect = { goTo(it) })
                }
            },
        ) { padding ->
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier.padding(padding),
            ) { index ->
                when (Page.entries[index]) {
                    Page.Queue -> QueueScreen(
                        state = playback,
                        onSkipTo = playerViewModel::skipTo,
                        onRemove = playerViewModel::removeAt,
                    )
                    Page.Player -> PlayerScreen(
                        state = playback,
                        positionMs = positionMs,
                        isFavorite = isFavorite,
                        artworkColor = artworkColor,
                        viewModel = playerViewModel,
                        onBrowseLibrary = { goTo(Page.Library) },
                    )
                    Page.Library -> LibraryScreen(
                        nowPlayingId = playback.current?.mediaId,
                        isPlaying = playback.isPlaying,
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBar(selected: Page, onSelect: (Page) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(60.dp),
    ) {
        Page.entries.forEach { page ->
            val isSelected = page == selected
            val tint by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                label = "navTint",
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(page) }
                    .semantics {
                        role = Role.Tab
                        this.selected = isSelected
                    },
            ) {
                Icon(page.icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(4.dp))
                Text(page.label, style = MaterialTheme.typography.labelSmall, color = tint)
            }
        }
    }
}
