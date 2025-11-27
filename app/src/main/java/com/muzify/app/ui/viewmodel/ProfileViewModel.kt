package com.muzify.app.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muzify.app.data.preferences.MusicFolderManager
import com.muzify.app.data.repository.PlaylistRepository
import com.muzify.app.data.repository.TrackRepository
import com.muzify.app.domain.usecase.ScanMediaUseCase
import com.muzify.app.domain.usecase.UpdateCoverArtUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val playlistRepository: PlaylistRepository,
    private val scanMediaUseCase: ScanMediaUseCase,
    private val updateCoverArtUseCase: UpdateCoverArtUseCase,
    private val musicFolderManager: MusicFolderManager
) : ViewModel() {

    data class ProfileStats(
        val totalSongs: Int = 0,
        val likedSongs: Int = 0,
        val playlistsCreated: Int = 0
    )

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _selectedFolderName = MutableStateFlow(musicFolderManager.getFolderName())
    val selectedFolderName: StateFlow<String?> = _selectedFolderName.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError.asStateFlow()

    val stats: StateFlow<ProfileStats> = combine(
        trackRepository.getTrackCount(),
        trackRepository.getLikedTrackCount(),
        playlistRepository.getPlaylistCount()
    ) { total, liked, playlists ->
        ProfileStats(
            totalSongs = total,
            likedSongs = liked,
            playlistsCreated = playlists
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileStats()
    )

    fun selectMusicFolder(uri: Uri) {
        Log.d(TAG, "selectMusicFolder: $uri")
        musicFolderManager.saveFolderUri(uri)
        _selectedFolderName.value = musicFolderManager.getFolderName()
        rescanMedia()
    }

    fun clearMusicFolder() {
        Log.d(TAG, "clearMusicFolder")
        musicFolderManager.clearFolder()
        _selectedFolderName.value = null
    }

    fun rescanMedia() {
        Log.d(TAG, "rescanMedia: Starting scan")
        if (musicFolderManager.getFolderUri() == null) {
            Log.e(TAG, "rescanMedia: No folder selected")
            _scanError.value = "Select a music folder first."
            return
        }
        viewModelScope.launch {
            _scanError.value = null
            _isScanning.value = true
            val result = scanMediaUseCase()
            if (result.isFailure) {
                val error = result.exceptionOrNull()
                Log.e(TAG, "rescanMedia: Scan failed", error)
                _scanError.value = error?.localizedMessage ?: "Scan failed"
            } else {
                Log.d(TAG, "rescanMedia: Scan successful, updating cover art")
                updateCoverArtUseCase()
            }
            _isScanning.value = false
        }
    }

    companion object {
        private const val TAG = "ProfileViewModel"
    }
}

