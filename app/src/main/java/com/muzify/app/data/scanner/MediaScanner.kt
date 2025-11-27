package com.muzify.app.data.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.muzify.app.data.model.Track
import com.muzify.app.data.preferences.MusicFolderManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicFolderManager: MusicFolderManager
) {

    suspend fun scanMediaFiles(): List<Track> = withContext(Dispatchers.IO) {
        val folderUri = musicFolderManager.getFolderUri()
        if (folderUri == null) {
            Log.e(TAG, "scanMediaFiles: No music folder selected")
            throw IllegalStateException("No music folder selected")
        }

        Log.d(TAG, "scanMediaFiles: Starting scan for URI: $folderUri")

        val root = DocumentFile.fromTreeUri(context, folderUri)
        if (root == null || !root.canRead()) {
            Log.e(TAG, "scanMediaFiles: Selected folder can’t be read or is null")
            throw IllegalStateException("Selected folder can’t be read")
        }

        val tracks = mutableListOf<Track>()
        traverseFolder(root, tracks)
        Log.d(TAG, "scanMediaFiles: Scan complete. Found ${tracks.size} tracks")
        tracks
    }

    private fun traverseFolder(file: DocumentFile, collector: MutableList<Track>) {
        if (!file.isDirectory) {
            if (isAudioFile(file)) {
                createTrackFromDocument(file)?.let {
                    collector.add(it)
                }
            }
            return
        }
        
        val files = file.listFiles()
        
        files.forEach { child ->
            traverseFolder(child, collector)
        }
    }

    private fun isAudioFile(file: DocumentFile): Boolean {
        if (!file.isFile) return false
        val name = file.name?.lowercase() ?: return false
        val type = file.type?.lowercase()
        
        // Check MIME type first if available
        if (type != null && type.startsWith("audio")) return true
        
        // Fallback to extension check
        return audioExtensions.any { name.endsWith(it) }
    }

    private fun createTrackFromDocument(documentFile: DocumentFile): Track? {
        return try {
            val uri = documentFile.uri
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set data source for ${documentFile.name}: ${e.message}")
                return null
            }

            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                    ?: 0L
            
            // Relaxed duration check - some valid files might have issues reporting duration
            // if (duration <= 0L) {
            //    retriever.release()
            //    return null
            // }

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: documentFile.name
                ?: "Unknown"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?: "Unknown Album"
            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull()

            val coverArtPath = saveCoverArt(retriever.embeddedPicture, uri.toString())
            retriever.release()

            Track(
                id = 0,
                path = uri.toString(),
                title = title,
                artist = artist,
                album = album,
                year = year,
                coverArtPath = coverArtPath,
                duration = duration
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating track from ${documentFile.name}", e)
            null
        }
    }

    private fun saveCoverArt(bytes: ByteArray?, key: String): String? {
        if (bytes == null) return null
        return try {
            val coverDir = File(context.cacheDir, "covers")
            if (!coverDir.exists()) {
                coverDir.mkdirs()
            }
            val fileName = key.hashCode().toString()
            val coverFile = File(coverDir, "$fileName.jpg")
            FileOutputStream(coverFile).use { it.write(bytes) }
            coverFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving cover art", e)
            null
        }
    }

    companion object {
        private const val TAG = "MediaScanner"
        private val audioExtensions = listOf(
            ".mp3",
            ".wav",
            ".aac",
            ".m4a",
            ".flac",
            ".ogg",
            ".wma"
        )
    }
}
