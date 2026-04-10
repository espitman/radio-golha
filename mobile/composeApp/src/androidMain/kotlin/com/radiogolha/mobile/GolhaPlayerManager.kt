package com.radiogolha.mobile

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.radiogolha.mobile.ui.home.TrackUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GolhaPlayerManager(context: Context) {
    private val player = ExoPlayer.Builder(context).build()
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
        scope.launch {
            while (true) {
                _currentPositionMs.value = player.currentPosition.coerceAtLeast(0L)
                _durationMs.value = player.duration.takeIf { it > 0L } ?: 0L
                delay(500)
            }
        }

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
            }
        )
    }

    fun play(track: TrackUiModel) {
        val audioUrl = track.audioUrl?.trim().orEmpty()
        if (audioUrl.isBlank()) return

        val isSameTrack = _currentTrack.value?.id == track.id
        _currentTrack.value = track

        if (isSameTrack) {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
            return
        }

        _isLoading.value = true
        _currentPositionMs.value = 0L
        _durationMs.value = 0L
        player.setMediaItem(MediaItem.fromUri(Uri.parse(audioUrl)))
        player.prepare()
        player.playWhenReady = true
    }

    fun togglePlayback() {
        val current = _currentTrack.value ?: return
        if (current.audioUrl.isNullOrBlank()) return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun release() {
        player.release()
    }
}
