package com.muzify.app.data.repository

import com.muzify.app.data.database.dao.PlaylistDao
import com.muzify.app.data.database.dao.PlaylistTrackDao
import com.muzify.app.data.database.entities.PlaylistEntity
import com.muzify.app.data.model.Playlist
import com.muzify.app.data.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val playlistTrackDao: PlaylistTrackDao
) {
    fun getAllPlaylists(): Flow<List<Playlist>> =
        playlistDao.getAllPlaylists().map { entities ->
            entities.map { Playlist.fromEntity(it) }
        }

    suspend fun getPlaylistById(id: Long): Playlist? =
        playlistDao.getPlaylistById(id)?.let { Playlist.fromEntity(it) }

    suspend fun createPlaylist(name: String): Long {
        val newPlaylistEntity = PlaylistEntity(name = name)
        return playlistDao.insertPlaylist(newPlaylistEntity)
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(playlist.toEntity())
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist.toEntity())
    }

    fun getTracksInPlaylist(playlistId: Long): Flow<List<Track>> =
        playlistTrackDao.getTracksInPlaylist(playlistId).map { entities ->
            entities.map { Track.fromEntity(it) }
        }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        val maxOrder = playlistTrackDao.getMaxOrder(playlistId) ?: -1
        val playlistTrack = com.muzify.app.data.database.entities.PlaylistTrackEntity(
            playlistId = playlistId,
            trackId = trackId,
            order = maxOrder + 1
        )
        playlistTrackDao.insertPlaylistTrack(playlistTrack)
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistTrackDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    fun getPlaylistCount(): Flow<Int> = playlistDao.getPlaylistCount()
}

