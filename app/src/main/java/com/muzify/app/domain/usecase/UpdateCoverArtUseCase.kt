package com.muzify.app.domain.usecase

import android.content.Context
import android.media.MediaMetadataRetriever
import com.muzify.app.data.repository.TrackRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class UpdateCoverArtUseCase @Inject constructor(
    private val trackRepository: TrackRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        val tracks = trackRepository.getAllTracksList()
        for (track in tracks) {
            if (track.coverArtPath == null) {
                val coverArtPath = extractCoverArt(track.id, track.path)
                if (coverArtPath != null) {
                    trackRepository.updateCoverArt(track.id, coverArtPath)
                }
            }
        }
    }

    private fun extractCoverArt(trackId: Long, filePath: String): String? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val picture = retriever.embeddedPicture
            retriever.release()

            if (picture != null) {
                val coverDir = File(context.cacheDir, "covers")
                if (!coverDir.exists()) {
                    coverDir.mkdirs()
                }
                val coverFile = File(coverDir, "${trackId}.jpg")
                FileOutputStream(coverFile).use { it.write(picture) }
                coverFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
