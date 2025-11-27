package com.muzify.app.ui.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.muzify.app.domain.usecase.GetRecentlyPlayedUseCase
import com.muzify.app.domain.usecase.ScanMediaUseCase
import com.muzify.app.data.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val getRecentlyPlayedUseCase: GetRecentlyPlayedUseCase,
    private val scanMediaUseCase: ScanMediaUseCase
) : AndroidViewModel(application) {

    private val _recentlyPlayed = MutableStateFlow<List<Track>>(emptyList())
    val recentlyPlayed: StateFlow<List<Track>> = _recentlyPlayed.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        loadRecentlyPlayed()
        // Trigger initial scan on first launch
        val prefs = PreferenceManager.getDefaultSharedPreferences(getApplication())
        if (!prefs.getBoolean("has_scanned", false)) {
            rescanMedia()
            prefs.edit().putBoolean("has_scanned", true).apply()
        }
    }

    private fun loadRecentlyPlayed() {
        viewModelScope.launch {
            getRecentlyPlayedUseCase(10).collect { tracks ->
                _recentlyPlayed.value = tracks
            }
        }
    }

    fun rescanMedia() {
        viewModelScope.launch {
            _isScanning.value = true
            scanMediaUseCase()
            _isScanning.value = false
        }
    }
}

