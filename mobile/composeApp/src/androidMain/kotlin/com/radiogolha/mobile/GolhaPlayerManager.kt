package com.radiogolha.mobile

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
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

class GolhaPlayerManager(context: Context) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val player: Player? get() = if (controllerFuture?.isDone == true) controllerFuture?.get() else null
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

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

    init {
        val sessionToken = SessionToken(context, ComponentName(context, GolhaPlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        
        controllerFuture?.addListener({
            val controller = controllerFuture?.get() ?: return@addListener
            setupPlayer(controller)
        }, MoreExecutors.directExecutor())

        scope.launch {
            while (true) {
                player?.let { p ->
                    _currentPositionMs.value = p.currentPosition.coerceAtLeast(0L)
                    _durationMs.value = p.duration.takeIf { it > 0L } ?: 0L
                    _isPlaying.value = p.isPlaying
                }
                delay(500)
            }
        }
    }

    private fun setupPlayer(player: Player) {
        player.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    _isLoading.value = playbackState == Player.STATE_BUFFERING
                    _durationMs.value = player.duration.takeIf { it > 0L } ?: 0L
                    if (playbackState == Player.STATE_ENDED) {
                        _isPlaying.value = false
                        _currentPositionMs.value = 0L
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    // This helps sync the current track if it was changed from outside (e.g. notification)
                    mediaItem?.mediaMetadata?.let { metadata ->
                        // We might not have the full TrackUiModel here, but we can update what we have
                        // or just rely on the fact that we usually trigger play from UI
                    }
                }
            }
        )
    }

    fun play(track: TrackUiModel) {
        val p = player ?: return
        val audioUrl = track.audioUrl?.trim().orEmpty()
        if (audioUrl.isBlank()) return

        val isSameTrack = _currentTrack.value?.id == track.id
        _currentTrack.value = track

        if (isSameTrack) {
            if (p.isPlaying) {
                p.pause()
            } else {
                p.play()
            }
            return
        }

        _isLoading.value = true
        _currentPositionMs.value = 0L
        _durationMs.value = 0L

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setArtworkUri(track.coverUrl?.let { Uri.parse(it) })
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(Uri.parse(audioUrl))
            .setMediaMetadata(mediaMetadata)
            .build()

        p.setMediaItem(mediaItem)
        p.prepare()
        p.playWhenReady = true
    }

    fun togglePlayback() {
        val p = player ?: return
        val current = _currentTrack.value ?: return
        if (current.audioUrl.isNullOrBlank()) return
        if (p.isPlaying) p.pause() else p.play()
    }

    fun release() {
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }
}
