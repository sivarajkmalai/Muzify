package com.muzify.app.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.muzify.app.data.model.Track
import com.muzify.app.domain.usecase.ToggleLikeTrackUseCase
import com.muzify.app.player.MusicService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val toggleLikeTrackUseCase: ToggleLikeTrackUseCase
) : AndroidViewModel(application) {

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _loopMode = MutableStateFlow(MusicService.LoopMode.NONE)
    val loopMode: StateFlow<MusicService.LoopMode> = _loopMode.asStateFlow()

    private val _isServiceBound = MutableStateFlow(false)
    val isServiceBound: StateFlow<Boolean> = _isServiceBound.asStateFlow()

    var musicService: MusicService? = null
        private set

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            _isServiceBound.value = true
            updatePlayerState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            _isServiceBound.value = false
        }
    }

    init {
        bindService()
    }

    private fun bindService() {
        val intent = Intent(getApplication(), MusicService::class.java)
        val context = getApplication<Application>()
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.startService(intent)
        }
    }

    fun playQueue(tracks: List<Track>, startIndex: Int = 0) {
        musicService?.setQueue(tracks, startIndex)
        updatePlayerState()
    }

    fun playPause() {
        musicService?.playPause()
        updatePlayerState()
    }

    fun playNext() {
        musicService?.playNext()
        updatePlayerState()
    }

    fun playPrevious() {
        musicService?.playPrevious()
        updatePlayerState()
    }

    fun seekTo(position: Long) {
        musicService?.seekTo(position)
    }

    fun skipForward() {
        musicService?.skipForward()
    }

    fun skipBackward() {
        musicService?.skipBackward()
    }

    fun setLoopMode(mode: MusicService.LoopMode) {
        musicService?.setLoopMode(mode)
        _loopMode.value = mode
    }

    fun toggleLike() {
        _currentTrack.value?.let { track ->
            viewModelScope.launch {
                toggleLikeTrackUseCase(track.id)
                _currentTrack.value = track.copy(liked = !track.liked)
            }
        }
    }

    fun updatePlayerState() {
        musicService?.let { service ->
            _currentTrack.value = service.getCurrentTrack()
            _isPlaying.value = service.isPlaying()
            _currentPosition.value = service.getCurrentPosition()
            _duration.value = service.getDuration()
            _loopMode.value = service.getLoopMode()
        }
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unbindService(serviceConnection)
    }
}

