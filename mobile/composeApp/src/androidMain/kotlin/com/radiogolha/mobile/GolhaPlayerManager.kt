package com.radiogolha.mobile

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.radiogolha.mobile.ui.home.TrackUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GolhaPlayerManager(private val context: Context) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val player: Player? get() = if (controllerFuture?.isDone == true) controllerFuture?.get() else null
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val prefs: SharedPreferences = context.getSharedPreferences("golha_playback_prefs", Context.MODE_PRIVATE)

    private val _currentTrack = MutableStateFlow<TrackUiModel?>(null)
    val currentTrack: StateFlow<TrackUiModel?> = _currentTrack

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs

    private var isRestoring = false

    init {
        loadFromPrefs()
        
        val sessionToken = SessionToken(context, ComponentName(context, GolhaPlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        
        controllerFuture?.addListener({
            val controller = controllerFuture?.get() ?: return@addListener
            setupPlayer(controller)
            syncWithPlayer(controller)
        }, MoreExecutors.directExecutor())

        scope.launch {
            var lastSavedPosition = 0L
            while (true) {
                player?.let { p ->
                    if (!isRestoring && p.playbackState != Player.STATE_IDLE && p.mediaItemCount > 0) {
                        val pos = p.currentPosition.coerceAtLeast(0L)
                        val dur = p.duration.coerceAtLeast(0L)
                        
                        _currentPositionMs.value = pos
                        if (dur > 0) _durationMs.value = dur
                        _isPlaying.value = p.isPlaying
                        
                        if (kotlin.math.abs(pos - lastSavedPosition) > 2000L) {
                            savePlaybackPosition(pos, dur, useCommit = false)
                            lastSavedPosition = pos
                        }
                    }
                }
                delay(500)
            }
        }
    }

    private fun loadFromPrefs() {
        val trackId = prefs.getLong("last_track_id", -1L)
        if (trackId == -1L) return

        val title = prefs.getString("last_track_title", "") ?: ""
        val artist = prefs.getString("last_track_artist", "") ?: ""
        val url = prefs.getString("last_track_url", "") ?: ""
        
        if (title.isBlank() || url.isBlank()) return

        _currentTrack.value = TrackUiModel(
            id = trackId,
            title = title,
            artist = artist,
            audioUrl = url,
            coverUrl = null
        )
        _currentPositionMs.value = prefs.getLong("last_position", 0L)
        _durationMs.value = prefs.getLong("last_duration", 0L)
    }

    private fun setupPlayer(player: Player) {
        player.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (!isPlaying) {
                        savePlaybackPosition(player.currentPosition, player.duration, useCommit = true)
                    }
                }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    _isLoading.value = playbackState == Player.STATE_BUFFERING
                    if (playbackState == Player.STATE_READY) {
                        isRestoring = false
                        _durationMs.value = player.duration.coerceAtLeast(0L)
                    }
                }
            }
        )
    }

    private fun syncWithPlayer(p: Player) {
        val track = _currentTrack.value ?: return
        val mediaId = p.currentMediaItem?.mediaId
        if (mediaId == track.id.toString()) return

        isRestoring = true
        val posToRestore = _currentPositionMs.value
        val mediaMetadata = createMetadata(track)
        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(Uri.parse(track.audioUrl))
            .setMediaMetadata(mediaMetadata)
            .build()

        // DIRECT INJECTION: Set media item AND start position in one atomic call
        p.setMediaItem(mediaItem, posToRestore)
        p.prepare()
        p.playWhenReady = false 
    }

    private fun savePlaybackState(track: TrackUiModel) {
        prefs.edit().apply {
            putLong("last_track_id", track.id)
            putString("last_track_title", track.title)
            putString("last_track_artist", track.artist)
            putString("last_track_url", track.audioUrl)
            apply()
        }
    }

    private fun savePlaybackPosition(pos: Long, duration: Long, useCommit: Boolean) {
        if (_currentTrack.value != null && pos >= 0L) {
            val editor = prefs.edit().putLong("last_position", pos)
            if (duration > 0) editor.putLong("last_duration", duration)
            if (useCommit) editor.commit() else editor.apply()
        }
    }

    fun play(track: TrackUiModel) {
        val p = player ?: return
        val audioUrl = track.audioUrl?.trim().orEmpty()
        if (audioUrl.isBlank()) return

        val isSameTrack = _currentTrack.value?.id == track.id
        _currentTrack.value = track
        savePlaybackState(track)

        if (isSameTrack) {
            if (p.isPlaying) p.pause() else p.play()
            return
        }

        _isLoading.value = true
        _currentPositionMs.value = 0L
        _durationMs.value = 0L

        val mediaMetadata = createMetadata(track)
        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(Uri.parse(audioUrl))
            .setMediaMetadata(mediaMetadata)
            .build()

        p.setMediaItem(mediaItem)
        p.prepare()
        p.playWhenReady = true
    }

    private fun createMetadata(track: TrackUiModel): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .apply {
                getPlaceholderArtworkBytes(context)?.let {
                    setArtworkData(it, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                }
            }
            .build()
    }

    fun togglePlayback() {
        val p = player ?: return
        val current = _currentTrack.value ?: return
        if (current.audioUrl.isNullOrBlank()) return
        if (p.isPlaying) {
             p.pause()
             savePlaybackPosition(p.currentPosition, p.duration, useCommit = true)
        } else {
             p.play()
        }
    }

    private fun getPlaceholderArtworkBytes(context: Context): ByteArray? {
        val resId = context.resources.getIdentifier("golha_artwork_placeholder", "drawable", context.packageName)
        if (resId == 0) return null
        val drawable = ContextCompat.getDrawable(context, resId) ?: return null
        val bitmap = if (drawable is BitmapDrawable) drawable.bitmap else {
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 512
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 512
            val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(b)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            b
        }
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun release() { controllerFuture?.let { MediaController.releaseFuture(it) } }
}
