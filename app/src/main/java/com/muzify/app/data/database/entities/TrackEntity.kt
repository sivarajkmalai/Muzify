package com.muzify.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
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
)

