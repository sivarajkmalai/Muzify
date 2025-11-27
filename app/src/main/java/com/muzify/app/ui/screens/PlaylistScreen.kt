package com.muzify.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.muzify.app.data.model.Playlist
import com.muzify.app.data.model.Track
import com.muzify.app.ui.components.TrackItem
import com.muzify.app.ui.util.PaletteHelper
import com.muzify.app.ui.viewmodel.PlaylistViewModel
import com.muzify.app.ui.viewmodel.PlayerViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    playlistId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    playerViewModel: PlayerViewModel,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val playlist by viewModel.playlist.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val allPlaylists by viewModel.allPlaylists.collectAsState()

    var showAddSongSheet by remember { mutableStateOf(false) }

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
        viewModel.loadAllPlaylists()
    }

    val gradientColors = remember(playlist?.coverArtPath) {
        mutableStateOf(
            listOf(
                androidx.compose.ui.graphics.Color(0xFF1DB954),
                androidx.compose.ui.graphics.Color(0xFF191414)
            )
        )
    }

    LaunchedEffect(playlist?.coverArtPath) {
        playlist?.coverArtPath?.let { path ->
            val colors = PaletteHelper.getGradientColors(path)
            gradientColors.value = colors
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist?.name ?: "Playlist") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = gradientColors.value
                        )
                    )
            ) {
                AsyncImage(
                    model = playlist?.coverArtPath ?: "",
                    contentDescription = playlist?.name,
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = playlist?.name ?: "",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "${tracks.size} songs",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { showAddSongSheet = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Song")
                    }
                    FloatingActionButton(
                        onClick = {
                            if (tracks.isNotEmpty()) {
                                playerViewModel.playQueue(tracks, 0)
                                onNavigateToPlayer()
                            }
                        }
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tracks) { track ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrackItem(
                            track = track,
                            onClick = {
                                playerViewModel.playQueue(tracks, tracks.indexOf(track))
                                onNavigateToPlayer()
                            },
                            modifier = Modifier.weight(1f),
                            isListMode = true
                        )
                        IconButton(
                            onClick = {
                                viewModel.removeTrackFromPlaylist(playlistId, track.id)
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    }
                }
            }
        }
    }

    if (showAddSongSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSongSheet = false }
        ) {
            val allTracks by viewModel.allTracks.collectAsState()
            
            LaunchedEffect(Unit) {
                viewModel.loadAllTracks()
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Add Songs",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allTracks) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.addTrackToPlaylist(playlistId, track.id)
                                    showAddSongSheet = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = track.coverArtPath,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = track.title, style = MaterialTheme.typography.bodyLarge)
                                Text(text = track.artist, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
