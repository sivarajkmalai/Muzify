package com.muzify.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.muzify.app.data.database.dao.PlaylistDao
import com.muzify.app.data.database.dao.PlaylistTrackDao
import com.muzify.app.data.database.dao.TrackDao
import com.muzify.app.data.database.entities.PlaylistEntity
import com.muzify.app.data.database.entities.PlaylistTrackEntity
import com.muzify.app.data.database.entities.TrackEntity

@Database(
    entities = [TrackEntity::class, PlaylistEntity::class, PlaylistTrackEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MuzifyDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTrackDao(): PlaylistTrackDao
}

