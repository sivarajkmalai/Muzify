package com.muzify.app.data.preferences

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicFolderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveFolderUri(uri: Uri) {
        Log.d(TAG, "saveFolderUri: $uri")
        val contentResolver = context.contentResolver
        val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        try {
            contentResolver.takePersistableUriPermission(uri, flag)
            Log.d(TAG, "saveFolderUri: Permission granted")
        } catch (e: SecurityException) {
            Log.w(TAG, "saveFolderUri: Permission already granted or failed", e)
        } catch (e: Exception) {
            Log.e(TAG, "saveFolderUri: Unexpected error taking permission", e)
        }
        prefs.edit().putString(KEY_FOLDER_URI, uri.toString()).apply()
    }

    fun clearFolder() {
        Log.d(TAG, "clearFolder")
        prefs.edit().remove(KEY_FOLDER_URI).apply()
    }

    fun getFolderUri(): Uri? {
        val uriString = prefs.getString(KEY_FOLDER_URI, null)
        Log.v(TAG, "getFolderUri: $uriString")
        return uriString?.let { Uri.parse(it) }
    }

    fun hasFolderSelected(): Boolean = getFolderUri() != null

    fun getFolderName(): String? =
        getFolderUri()?.let { DocumentFile.fromTreeUri(context, it)?.name }

    companion object {
        private const val TAG = "MusicFolderManager"
        private const val PREF_NAME = "music_folder_prefs"
        private const val KEY_FOLDER_URI = "folder_uri"
    }
}


