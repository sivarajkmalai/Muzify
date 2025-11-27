package com.muzify.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muzify.app.data.model.Playlist
import com.muzify.app.data.model.Track
import com.muzify.app.data.repository.PlaylistRepository
import com.muzify.app.data.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val trackRepository: TrackRepository
) : ViewModel() {

    private val _playlist = MutableStateFlow<Playlist?>(null)
    val playlist: StateFlow<Playlist?> = _playlist.asStateFlow()

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _allPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val allPlaylists: StateFlow<List<Playlist>> = _allPlaylists.asStateFlow()

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            _playlist.value = playlistRepository.getPlaylistById(playlistId)
            playlistRepository.getTracksInPlaylist(playlistId).collect { tracks ->
                _tracks.value = tracks
            }
        }
    }

    fun loadAllPlaylists() {
        viewModelScope.launch {
            playlistRepository.getAllPlaylists().collect { playlists ->
                _allPlaylists.value = playlists
            }
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            playlistRepository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            playlistRepository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }
}

