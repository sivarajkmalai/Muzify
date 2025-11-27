package com.muzify.app.data.model

import com.muzify.app.data.database.entities.PlaylistEntity

data class Playlist(
    val id: Long = 0,
    val name: String,
    val coverArtPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toEntity(): PlaylistEntity {
        return PlaylistEntity(
            id = this.id,
            name = this.name,
            coverArtPath = this.coverArtPath,
            createdAt = this.createdAt
        )
    }

    companion object {
        fun fromEntity(entity: PlaylistEntity) = Playlist(
            id = entity.id,
            name = entity.name,
            coverArtPath = entity.coverArtPath,
            createdAt = entity.createdAt
        )
    }
}
