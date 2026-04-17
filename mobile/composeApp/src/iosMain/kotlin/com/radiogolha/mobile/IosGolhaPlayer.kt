package com.radiogolha.mobile

import androidx.compose.runtime.*
import com.radiogolha.mobile.ui.home.TrackUiModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.AVFoundation.*
import platform.Foundation.*
import platform.AudioToolbox.*
import platform.AVFAudio.*

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

    private var timeObserver: Any? = null

    init {
        setupAudioSession()
        observePlayback()
    }

    private fun setupAudioSession() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, error = null)
    }

    private fun observePlayback() {
        // Observe status (loading)
        avPlayer.addObserver(observer = { _ ->
            _isLoading.value = avPlayer.status == AVPlayerStatusUnknown
        }, forKeyPath = "status", options = NSKeyValueObservingOptionNew, context = null)

        // Observe periodic time updates
        val interval = CMTimeMake(value = 1, timescale = 2) // 0.5 sec
        timeObserver = avPlayer.addPeriodicTimeObserverForInterval(interval, queue = null) { time ->
            val seconds = CMTimeGetSeconds(time)
            _currentPositionMs.value = (seconds * 1000).toLong()
            
            val duration = avPlayer.currentItem?.duration
            if (duration != null) {
                val durationSeconds = CMTimeGetSeconds(duration)
                if (!durationSeconds.isNaN()) {
                    _durationMs.value = (durationSeconds * 1000).toLong()
                }
            }
            
            _isPlaying.value = avPlayer.rate > 0
        }
    }

    override fun play(track: TrackUiModel) {
        val urlString = track.audioUrl ?: return
        val url = NSURL.URLWithString(urlString) ?: return
        
        _currentTrack.value = track
        _isLoading.value = true
        
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
