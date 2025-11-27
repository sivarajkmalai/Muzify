package com.muzify.app.data.model

import com.muzify.app.data.database.entities.TrackEntity

data class Track(
    val id: Long = 0,
    val path: String,
    val title: String,
    val artist: String,
    val album: String,
    val year: Int? = null,
    val coverArtPath: String? = null,
    val duration: Long,
    val liked: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long? = null
) {
    fun toEntity() = TrackEntity(
        id = id,
        path = path,
        title = title,
        artist = artist,
        album = album,
        year = year,
        coverArtPath = coverArtPath,
        duration = duration,
        liked = liked,
        playCount = playCount,
        lastPlayed = lastPlayed
    )

    companion object {
        fun fromEntity(entity: TrackEntity) = Track(
            id = entity.id,
            path = entity.path,
            title = entity.title,
            artist = entity.artist,
            album = entity.album,
            year = entity.year,
            coverArtPath = entity.coverArtPath,
            duration = entity.duration,
            liked = entity.liked,
            playCount = entity.playCount,
            lastPlayed = entity.lastPlayed
        )
    }
}

