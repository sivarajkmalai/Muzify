package com.muzify.app.data.database.dao

import androidx.room.*
import com.muzify.app.data.database.entities.PlaylistTrackEntity
import com.muzify.app.data.database.entities.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistTrackDao {
    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_tracks pt ON t.id = pt.trackId
        WHERE pt.playlistId = :playlistId
        ORDER BY pt.`order` ASC
    """)
    fun getTracksInPlaylist(playlistId: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY `order` ASC")
    suspend fun getPlaylistTracks(playlistId: Long): List<PlaylistTrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrack(playlistTrack: PlaylistTrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTracks(playlistTracks: List<PlaylistTrackEntity>)

    @Delete
    suspend fun deletePlaylistTrack(playlistTrack: PlaylistTrackEntity)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun deleteAllTracksFromPlaylist(playlistId: Long)

    @Query("SELECT MAX(`order`) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getMaxOrder(playlistId: Long): Int?
}

