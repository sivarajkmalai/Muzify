package com.muzify.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.muzify.app.R
import com.muzify.app.data.model.Track
import com.muzify.app.ui.components.PlaylistItem
import com.muzify.app.ui.components.TrackItem
import com.muzify.app.ui.viewmodel.LibraryViewModel
import kotlinx.coroutines.delay

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToPlaylist: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: com.muzify.app.ui.viewmodel.PlayerViewModel
) {
    val allTracks by viewModel.allTracks.collectAsState()
    val likedTracks by viewModel.likedTracks.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val deletedPlaylist by viewModel.deletedPlaylist.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    val hasFolderSelected by viewModel.hasFolderSelected.collectAsState()

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(deletedPlaylist) {
        if (deletedPlaylist != null) {
            showSnackbar = true
            delay(3000)
            showSnackbar = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshFolderStatus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.library)) },
                actions = {
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (viewMode == LibraryViewModel.ViewMode.LIST) Icons.Default.GridView else Icons.Default.List,
                            contentDescription = "Toggle view"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreatePlaylistDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_playlist))
            }
        },
        snackbarHost = {
            if (showSnackbar && deletedPlaylist != null) {
                Snackbar(
                    action = {
                        TextButton(onClick = {
                            viewModel.undoDeletePlaylist()
                            showSnackbar = false
                        }) {
                            Text(text = stringResource(R.string.undo))
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.playlist_deleted))
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                ScanStatusRow(
                    scanState = scanState,
                    hasFolder = hasFolderSelected,
                    onScan = { viewModel.scanLibrary() }
                )
            }

            item {
                TrackSection(
                    title = stringResource(R.string.downloaded_songs),
                    tracks = allTracks,
                    viewMode = viewMode,
                    emptyMessage = stringResource(R.string.no_songs_found),
                    onTrackClick = { track ->
                        playerViewModel.playQueue(allTracks, allTracks.indexOf(track))
                    }
                )
            }

            item {
                TrackSection(
                    title = stringResource(R.string.liked_songs),
                    tracks = likedTracks,
                    viewMode = viewMode,
                    emptyMessage = stringResource(R.string.no_liked_songs),
                    onTrackClick = { track ->
                        playerViewModel.playQueue(likedTracks, likedTracks.indexOf(track))
                    }
                )
            }

            item {
                Text(
                    text = stringResource(R.string.playlists),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (playlists.isEmpty()) {
                    EmptyStateCard(message = stringResource(R.string.no_playlists))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        playlists.forEach { playlist ->
                            PlaylistItem(
                                playlist = playlist,
                                onClick = { onNavigateToPlaylist(playlist.id) },
                                onDelete = { viewModel.deletePlaylist(playlist) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text(text = stringResource(R.string.create_playlist)) },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text(text = stringResource(R.string.enter_playlist_name)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            viewModel.createPlaylist(playlistName.trim())
                            playlistName = ""
                            showCreatePlaylistDialog = false
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.create))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun ScanStatusRow(
    scanState: LibraryViewModel.ScanState,
    hasFolder: Boolean,
    onScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.scan_library_title),
                style = MaterialTheme.typography.titleMedium
            )
            when (scanState) {
                LibraryViewModel.ScanState.Idle -> {
                    val message = if (hasFolder) {
                        stringResource(R.string.scan_library_hint)
                    } else {
                        stringResource(R.string.folder_not_selected)
                    }
                    Text(text = message, style = MaterialTheme.typography.bodySmall)
                }
                LibraryViewModel.ScanState.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        text = stringResource(R.string.scanning_library),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                is LibraryViewModel.ScanState.Success -> {
                    Text(
                        text = stringResource(R.string.scan_result, scanState.newItems),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                is LibraryViewModel.ScanState.Error -> {
                    Text(
                        text = scanState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Button(
                onClick = onScan,
                enabled = hasFolder && scanState != LibraryViewModel.ScanState.Loading
            ) {
                Text(text = stringResource(R.string.rescan_media))
            }
            if (!hasFolder) {
                Text(
                    text = stringResource(R.string.select_music_folder_instruction),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun TrackSection(
    title: String,
    tracks: List<Track>,
    viewMode: LibraryViewModel.ViewMode,
    emptyMessage: String,
    onTrackClick: (Track) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )
        if (tracks.isEmpty()) {
            EmptyStateCard(message = emptyMessage)
        } else {
            if (viewMode == LibraryViewModel.ViewMode.TILE) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(tracks) { track ->
                        TrackItem(
                            track = track,
                            onClick = { onTrackClick(track) }
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tracks.forEach { track ->
                        TrackItem(
                            track = track,
                            onClick = { onTrackClick(track) },
                            isListMode = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
