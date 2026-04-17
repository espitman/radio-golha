package com.radiogolha.mobile

import androidx.compose.runtime.Composable
import com.radiogolha.mobile.ui.home.TrackUiModel
import kotlinx.coroutines.flow.StateFlow

interface GolhaPlayer {
    val currentTrack: StateFlow<TrackUiModel?>
    val isPlaying: StateFlow<Boolean>
    val isLoading: StateFlow<Boolean>
    val currentPositionMs: StateFlow<Long>
    val durationMs: StateFlow<Long>

    fun play(track: TrackUiModel)
    fun togglePlayback()
    fun seekTo(positionMs: Long)
}

@Composable
expect fun rememberGolhaPlayer(): GolhaPlayer
