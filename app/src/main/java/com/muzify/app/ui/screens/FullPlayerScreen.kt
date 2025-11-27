package com.muzify.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.muzify.app.player.MusicService
import com.muzify.app.ui.theme.MuzifyTheme
import com.muzify.app.ui.util.PaletteHelper
import com.muzify.app.ui.viewmodel.PlaylistViewModel
import com.muzify.app.ui.viewmodel.PlayerViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    viewModel: PlayerViewModel,
    onDismiss: () -> Unit,
    playlistViewModel: PlaylistViewModel = hiltViewModel()
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val loopMode by viewModel.loopMode.collectAsState()

    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val allPlaylists by playlistViewModel.allPlaylists.collectAsState()

    var gradientColors by remember {
        mutableStateOf(
            listOf(
                androidx.compose.ui.graphics.Color(0xFF1DB954),
                androidx.compose.ui.graphics.Color(0xFF191414)
            )
        )
    }

    LaunchedEffect(currentTrack?.coverArtPath) {
        currentTrack?.coverArtPath?.let { path ->
            gradientColors = PaletteHelper.getGradientColors(path)
        }
    }

    LaunchedEffect(Unit) {
        playlistViewModel.loadAllPlaylists()
    }

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updatePlayerState()
            delay(100)
        }
    }
    MuzifyTheme(gradientColors = gradientColors) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = gradientColors
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Minimize",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                    Text(
                        text = "Now Playing",
                        style = MaterialTheme.typography.titleLarge,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                    IconButton(onClick = { showAddToPlaylistSheet = true }) {
                        Icon(
                            Icons.Default.PlaylistAdd,
                            contentDescription = "Add to Playlist",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }

                // Cover Art
                AsyncImage(
                    model = currentTrack?.coverArtPath ?: "",
                    contentDescription = currentTrack?.title,
                    modifier = Modifier
                        .size(320.dp)
                        .padding(vertical = 32.dp),
                    contentScale = ContentScale.Crop
                )

                // Track Info
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentTrack?.title ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                    Text(
                        text = currentTrack?.artist ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                    )
                }

                // Seekbar
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Slider(
                        value = currentPosition.toFloat(),
                        onValueChange = { viewModel.seekTo(it.toLong()) },
                        valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Text(
                            text = formatTime(duration),
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }

                // Controls
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 10s Backward
                        IconButton(onClick = { viewModel.skipBackward() }) {
                            Icon(
                                Icons.Default.Replay10,
                                contentDescription = "Skip Backward",
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Previous
                        IconButton(onClick = { viewModel.playPrevious() }) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        // Play/Pause
                        FloatingActionButton(
                            onClick = { viewModel.playPause() },
                            modifier = Modifier.size(72.dp),
                            containerColor = androidx.compose.ui.graphics.Color.White,
                            contentColor = androidx.compose.ui.graphics.Color.Black
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Next
                        IconButton(onClick = { viewModel.playNext() }) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        // 10s Forward
                        IconButton(onClick = { viewModel.skipForward() }) {
                            Icon(
                                Icons.Default.Forward10,
                                contentDescription = "Skip Forward",
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val nextMode = when (loopMode) {
                                    MusicService.LoopMode.NONE -> MusicService.LoopMode.ALL
                                    MusicService.LoopMode.ALL -> MusicService.LoopMode.ONE
                                    MusicService.LoopMode.ONE -> MusicService.LoopMode.NONE
                                }
                                viewModel.setLoopMode(nextMode)
                            }
                        ) {
                            Icon(
                                when (loopMode) {
                                    MusicService.LoopMode.NONE -> Icons.Default.Repeat
                                    MusicService.LoopMode.ALL -> Icons.Default.Repeat
                                    MusicService.LoopMode.ONE -> Icons.Default.RepeatOne
                                },
                                contentDescription = "Loop",
                                tint = if (loopMode == MusicService.LoopMode.NONE)
                                    androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f)
                                else
                                    androidx.compose.ui.graphics.Color.Green
                            )
                        }

                        IconButton(onClick = { viewModel.toggleLike() }) {
                            Icon(
                                if (currentTrack?.liked == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (currentTrack?.liked == true)
                                    androidx.compose.ui.graphics.Color.Green
                                else
                                    androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        IconButton(onClick = { showAddToPlaylistSheet = true }) {
                            Icon(
                                Icons.Default.PlaylistAdd,
                                contentDescription = "Add to Playlist",
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }
            }
        }

        if (showAddToPlaylistSheet && currentTrack != null) {
            ModalBottomSheet(
                onDismissRequest = { showAddToPlaylistSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Add to Playlist",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    allPlaylists.forEach { playlist ->
                        ListItem(
                            headlineContent = { Text(playlist.name) },
                            modifier = Modifier.clickable {
                                currentTrack?.let { track ->
                                    playlistViewModel.addTrackToPlaylist(playlist.id, track.id)
                                }
                                showAddToPlaylistSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

