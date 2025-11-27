package com.muzify.app.domain.usecase

import com.muzify.app.data.repository.TrackRepository
import com.muzify.app.data.scanner.MediaScanner
import javax.inject.Inject

class ScanMediaUseCase @Inject constructor(
    private val mediaScanner: MediaScanner,
    private val trackRepository: TrackRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return try {
            val tracks = mediaScanner.scanMediaFiles()
            trackRepository.upsertScannedTracks(tracks)
            Result.success(tracks.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

