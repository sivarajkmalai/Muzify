package com.muzify.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.muzify.app.data.model.Playlist
import com.muzify.app.data.model.Track
import com.muzify.app.data.repository.PlaylistRepository
import com.muzify.app.data.repository.TrackRepository
import com.muzify.app.domain.usecase.ScanMediaUseCase
import com.muzify.app.data.preferences.MusicFolderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    application: Application,
    private val trackRepository: TrackRepository,
    private val playlistRepository: PlaylistRepository,
    private val scanMediaUseCase: ScanMediaUseCase,
    private val musicFolderManager: MusicFolderManager
) : AndroidViewModel(application) {

    private val _allTracks = MutableStateFlow<List<Track>>(emptyList())
    val allTracks: StateFlow<List<Track>> = _allTracks.asStateFlow()

    private val _likedTracks = MutableStateFlow<List<Track>>(emptyList())
    val likedTracks: StateFlow<List<Track>> = _likedTracks.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.TILE)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _deletedPlaylist = MutableStateFlow<Playlist?>(null)
    val deletedPlaylist: StateFlow<Playlist?> = _deletedPlaylist.asStateFlow()

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _hasFolderSelected = MutableStateFlow(musicFolderManager.hasFolderSelected())
    val hasFolderSelected: StateFlow<Boolean> = _hasFolderSelected.asStateFlow()

    enum class ViewMode {
        LIST, TILE
    }

    init {
        observeTracks()
        observeLikedTracks()
        observePlaylists()
    }

    private fun observeTracks() {
        viewModelScope.launch {
            trackRepository.getAllTracks().collect { tracks ->
                val wasEmpty = _allTracks.value.isEmpty()
                _allTracks.value = tracks
                if (wasEmpty && tracks.isEmpty() && _scanState.value == ScanState.Idle && musicFolderManager.hasFolderSelected()) {
                    scanLibrary()
                }
            }
        }
    }

    private fun observeLikedTracks() {
        viewModelScope.launch {
            trackRepository.getLikedTracks().collect { tracks ->
                _likedTracks.value = tracks
            }
        }
    }

    private fun observePlaylists() {
        viewModelScope.launch {
            playlistRepository.getAllPlaylists().collect { playlists ->
                _playlists.value = playlists
            }
        }
    }

    fun toggleViewMode() {
        _viewMode.value = if (_viewMode.value == ViewMode.LIST) ViewMode.TILE else ViewMode.LIST
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlist)
            _deletedPlaylist.value = playlist
        }
    }

    fun undoDeletePlaylist() {
        _deletedPlaylist.value?.let { playlist ->
            viewModelScope.launch {
                playlistRepository.createPlaylist(playlist.name)
                _deletedPlaylist.value = null
            }
        }
    }

    fun refreshFolderStatus() {
        _hasFolderSelected.value = musicFolderManager.hasFolderSelected()
    }

    fun scanLibrary() {
        if (_scanState.value == ScanState.Loading) return
        if (!musicFolderManager.hasFolderSelected()) {
            _scanState.value = ScanState.Error("Select a music folder in Profile to enable scanning.")
            _hasFolderSelected.value = false
            return
        }
        viewModelScope.launch {
            _hasFolderSelected.value = true
            _scanState.value = ScanState.Loading
            val result = scanMediaUseCase()
            _scanState.value = result.fold(
                onSuccess = { count -> ScanState.Success(count) },
                onFailure = { error -> ScanState.Error(error.message ?: "Scan failed") }
            )
        }
    }

    sealed interface ScanState {
        object Idle : ScanState
        object Loading : ScanState
        data class Success(val newItems: Int) : ScanState
        data class Error(val message: String) : ScanState
    }
}

