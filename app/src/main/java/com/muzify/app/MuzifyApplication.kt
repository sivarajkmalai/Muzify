package com.muzify.app

import android.app.Application
import androidx.preference.PreferenceManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MuzifyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initial scan will be triggered from HomeViewModel on first launch
    }
}

