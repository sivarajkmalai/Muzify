package com.muzify.app.domain.usecase

import com.muzify.app.data.model.Track
import com.muzify.app.data.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentlyPlayedUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    operator fun invoke(limit: Int = 10): Flow<List<Track>> =
        trackRepository.getRecentlyPlayed(limit)
}

