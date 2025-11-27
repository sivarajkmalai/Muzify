package com.muzify.app.di

import android.content.Context
import com.muzify.app.data.preferences.MusicFolderManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMusicFolderManager(
        @ApplicationContext context: Context
    ): MusicFolderManager = MusicFolderManager(context)
}


