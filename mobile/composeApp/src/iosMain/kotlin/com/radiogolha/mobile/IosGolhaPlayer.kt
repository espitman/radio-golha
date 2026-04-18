@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.radiogolha.mobile

import androidx.compose.runtime.*
import com.radiogolha.mobile.ui.home.TrackUiModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.AVFoundation.*
import platform.Foundation.*
import platform.AVFAudio.*
import platform.CoreMedia.*

@Composable
actual fun rememberGolhaPlayer(): GolhaPlayer {
    val player = remember { IosGolhaPlayer() }
    return player
}

class IosGolhaPlayer : GolhaPlayer {
    private val avPlayer = AVPlayer()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _currentTrack = MutableStateFlow<TrackUiModel?>(null)
    override val currentTrack: StateFlow<TrackUiModel?> = _currentTrack

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentPositionMs = MutableStateFlow(0L)
    override val currentPositionMs: StateFlow<Long> = _currentPositionMs

    private val _durationMs = MutableStateFlow(0L)
    override val durationMs: StateFlow<Long> = _durationMs

    init {
        setupAudioSession()
        startPolling()
    }

    private fun setupAudioSession() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, error = null)
    }

    private fun startPolling() {
        scope.launch {
            while (true) {
                _isPlaying.value = avPlayer.rate > 0
                _isLoading.value = avPlayer.currentItem?.status == AVPlayerItemStatusUnknown

                val currentItem = avPlayer.currentItem
                if (currentItem != null) {
                    val currentTimeSec = CMTimeGetSeconds(avPlayer.currentTime())
                    if (!currentTimeSec.isNaN()) {
                        _currentPositionMs.value = (currentTimeSec * 1000).toLong()
                    }
                    val durationSec = CMTimeGetSeconds(currentItem.duration)
                    if (!durationSec.isNaN() && durationSec > 0) {
                        _durationMs.value = (durationSec * 1000).toLong()
                    }
                }
                delay(500)
            }
        }
    }

    override fun play(track: TrackUiModel) {
        val urlString = track.audioUrl ?: return
        val url = NSURL.URLWithString(urlString) ?: return
        
        _currentTrack.value = track
        _isLoading.value = true
        
        // Record playback in local database
        try {
            RustCoreBridge.recordPlayback(com.radiogolha.mobile.ui.home.requireUserDbPath(), track.id)
        } catch (e: Exception) {
            println("GOLHA: Failed to record playback: ${e.message}")
        }

        val playerItem = AVPlayerItem(uRL = url)
        avPlayer.replaceCurrentItemWithPlayerItem(playerItem)
        avPlayer.play()
    }

    override fun togglePlayback() {
        if (avPlayer.rate > 0) {
            avPlayer.pause()
        } else {
            avPlayer.play()
        }
        _isPlaying.value = avPlayer.rate > 0
    }

    override fun seekTo(positionMs: Long) {
        val time = CMTimeMake(value = positionMs, timescale = 1000)
        avPlayer.seekToTime(time)
    }
}
