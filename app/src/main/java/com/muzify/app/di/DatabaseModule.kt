package com.muzify.app.di

import android.content.Context
import androidx.room.Room
import com.muzify.app.data.database.MuzifyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MuzifyDatabase {
        return Room.databaseBuilder(
            context,
            MuzifyDatabase::class.java,
            "muzify_database"
        ).build()
    }

    @Provides
    fun provideTrackDao(database: MuzifyDatabase) = database.trackDao()

    @Provides
    fun providePlaylistDao(database: MuzifyDatabase) = database.playlistDao()

    @Provides
    fun providePlaylistTrackDao(database: MuzifyDatabase) = database.playlistTrackDao()
}

