package com.muzify.app.data.repository

import com.muzify.app.data.database.dao.TrackDao
import com.muzify.app.data.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepository @Inject constructor(
    private val trackDao: TrackDao
) {
    fun getRecentlyPlayed(limit: Int = 10): Flow<List<Track>> =
        trackDao.getRecentlyPlayed(limit).map { entities ->
            entities.map { Track.fromEntity(it) }
        }

    fun getAllTracks(): Flow<List<Track>> =
        trackDao.getAllTracks().map { entities ->
            entities.map { Track.fromEntity(it) }
        }

    suspend fun getAllTracksList(): List<Track> {
        return trackDao.getAllTracksList().map { Track.fromEntity(it) }
    }

    fun getLikedTracks(): Flow<List<Track>> =
        trackDao.getLikedTracks().map { entities ->
            entities.map { Track.fromEntity(it) }
        }

    suspend fun getTrackById(id: Long): Track? =
        trackDao.getTrackById(id)?.let { Track.fromEntity(it) }

    suspend fun getTrackByPath(path: String): Track? =
        trackDao.getTrackByPath(path)?.let { Track.fromEntity(it) }

    suspend fun insertTrack(track: Track): Long =
        trackDao.insertTrack(track.toEntity())

    suspend fun insertTracks(tracks: List<Track>) {
        trackDao.insertTracks(tracks.map { it.toEntity() })
    }

    suspend fun upsertScannedTracks(tracks: List<Track>) = withContext(Dispatchers.IO) {
        tracks.forEach { track ->
            val existing = trackDao.getTrackByPath(track.path)
            val entity = track.toEntity().copy(
                id = existing?.id ?: 0,
                liked = existing?.liked ?: track.liked,
                playCount = existing?.playCount ?: track.playCount,
                lastPlayed = existing?.lastPlayed
            )
            trackDao.insertTrack(entity)
        }
    }

    suspend fun updateTrack(track: Track) {
        trackDao.updateTrack(track.toEntity())
    }

    suspend fun updateCoverArt(id: Long, coverArtPath: String) {
        trackDao.updateCoverArt(id, coverArtPath)
    }

    suspend fun toggleLike(id: Long) {
        val track = trackDao.getTrackById(id) ?: return
        trackDao.updateLiked(id, !track.liked)
    }

    suspend fun incrementPlayCount(id: Long) {
        trackDao.incrementPlayCount(id)
    }

    fun getTrackCount(): Flow<Int> = trackDao.getTrackCount()

    fun getLikedTrackCount(): Flow<Int> = trackDao.getLikedTrackCount()

    suspend fun deleteTrack(track: Track) {
        trackDao.deleteTrack(track.toEntity())
    }
}

