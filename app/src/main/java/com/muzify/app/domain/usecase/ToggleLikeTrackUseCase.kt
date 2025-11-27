package com.muzify.app.domain.usecase

import com.muzify.app.data.repository.TrackRepository
import javax.inject.Inject

class ToggleLikeTrackUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    suspend operator fun invoke(trackId: Long) {
        trackRepository.toggleLike(trackId)
    }
}

