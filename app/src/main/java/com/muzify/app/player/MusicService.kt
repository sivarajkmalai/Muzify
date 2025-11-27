package com.muzify.app.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.muzify.app.MainActivity
import com.muzify.app.R
import com.muzify.app.data.model.Track
import com.muzify.app.data.repository.TrackRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaSessionService() {
    @Inject
    lateinit var trackRepository: TrackRepository

    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null
    private val binder = MusicBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isForegroundServiceStarted = false

    private val notificationManager: NotificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }

    private var currentQueue: List<Track> = emptyList()
    private var currentIndex: Int = 0
    private var loopMode: LoopMode = LoopMode.NONE

    enum class LoopMode {
        NONE, ONE, ALL
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Service created")
        createNotificationChannel()
        
        // Immediate foreground promotion to prevent system kill
        try {
            val notification = buildNotification(null)
            startForeground(NOTIFICATION_ID, notification)
            isForegroundServiceStarted = true
            Log.d(TAG, "onCreate: Started foreground service immediately")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Failed to start foreground service", e)
        }

        initializePlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureForegroundNotification()
        return START_STICKY
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        handleTrackEnd()
                    }
                }
            })
        }

        mediaSession = MediaSession.Builder(this, exoPlayer!!).build()
    }

    private fun handleTrackEnd() {
        when (loopMode) {
            LoopMode.NONE -> playNext()
            LoopMode.ONE -> exoPlayer?.seekTo(0)?.let { exoPlayer?.play() }
            LoopMode.ALL -> playNext()
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    fun setQueue(tracks: List<Track>, startIndex: Int = 0) {
        serviceScope.launch(Dispatchers.Default) {
            try {
                val mediaItems = tracks.map { track ->
                    MediaItem.fromUri(track.path)
                }
                withContext(Dispatchers.Main) {
                    currentQueue = tracks
                    currentIndex = startIndex
                    exoPlayer?.setMediaItems(mediaItems)
                    exoPlayer?.prepare()
                    playTrack(startIndex)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playTrack(index: Int) {
        if (index in currentQueue.indices) {
            currentIndex = index
            exoPlayer?.seekTo(index, 0)
            exoPlayer?.play()
            ensureForegroundNotification(currentQueue.getOrNull(index))
            currentQueue.getOrNull(index)?.let { track ->
                serviceScope.launch {
                    trackRepository.incrementPlayCount(track.id)
                }
            }
        }
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun playPause() {
        exoPlayer?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun playNext() {
        when (loopMode) {
            LoopMode.ALL -> {
                val nextIndex = (currentIndex + 1) % currentQueue.size
                playTrack(nextIndex)
            }
            else -> {
                if (currentIndex < currentQueue.size - 1) {
                    playTrack(currentIndex + 1)
                }
            }
        }
    }

    fun playPrevious() {
        if (exoPlayer?.currentPosition ?: 0 > 3000) {
            exoPlayer?.seekTo(0)
        } else {
            val prevIndex = if (currentIndex > 0) currentIndex - 1 else currentQueue.size - 1
            playTrack(prevIndex)
        }
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    fun skipForward() {
        exoPlayer?.let {
            it.seekTo((it.currentPosition + 10000).coerceAtMost(it.duration))
        }
    }

    fun skipBackward() {
        exoPlayer?.let {
            it.seekTo((it.currentPosition - 10000).coerceAtLeast(0))
        }
    }

    fun setLoopMode(mode: LoopMode) {
        loopMode = mode
        exoPlayer?.repeatMode = when (mode) {
            LoopMode.NONE -> Player.REPEAT_MODE_OFF
            LoopMode.ONE -> Player.REPEAT_MODE_ONE
            LoopMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    fun getCurrentTrack(): Track? = currentQueue.getOrNull(currentIndex)
    fun getCurrentPosition(): Long = exoPlayer?.currentPosition ?: 0
    fun getDuration(): Long = exoPlayer?.duration ?: 0
    fun isPlaying(): Boolean = exoPlayer?.isPlaying ?: false
    fun getCurrentIndex(): Int = currentIndex
    fun getQueue(): List<Track> = currentQueue
    fun getLoopMode(): LoopMode = loopMode

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Player",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(track: Track?): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            pendingIntentFlags
        )

        val title = track?.title ?: getString(R.string.app_name)
        val content = track?.artist ?: getString(R.string.recently_played)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun ensureForegroundNotification(track: Track? = getCurrentTrack()) {
        val notification = buildNotification(track)
        try {
            startForeground(NOTIFICATION_ID, notification)
            isForegroundServiceStarted = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        isForegroundServiceStarted = false
        mediaSession?.release()
        exoPlayer?.release()
        exoPlayer = null
        mediaSession = null
        try {
            serviceScope.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val CHANNEL_ID = "music_player_channel"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "MusicService"
    }
}

