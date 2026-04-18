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
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillTerminateNotification

@Composable
actual fun rememberGolhaPlayer(): GolhaPlayer {
    val player = remember { IosGolhaPlayer() }
    return player
}

class IosGolhaPlayer : GolhaPlayer {
    private val avPlayer = AVPlayer()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val prefs = NSUserDefaults.standardUserDefaults
    private var lastSavedPositionMs = 0L
    
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
        loadFromPrefs()
        restorePlayerFromSavedState()
        observeAppLifecycle()
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
                        val positionMs = (currentTimeSec * 1000).toLong().coerceAtLeast(0L)
                        _currentPositionMs.value = positionMs
                        if (_currentTrack.value != null && kotlin.math.abs(positionMs - lastSavedPositionMs) >= 2000L) {
                            savePlaybackPosition(positionMs, _durationMs.value)
                            lastSavedPositionMs = positionMs
                        }
                    }
                    val durationSec = CMTimeGetSeconds(currentItem.duration)
                    if (!durationSec.isNaN() && durationSec > 0) {
                        val durationMs = (durationSec * 1000).toLong()
                        _durationMs.value = durationMs
                    }
                }
                delay(500)
            }
        }
    }

    override fun play(track: TrackUiModel) {
        val urlString = track.audioUrl ?: return
        val url = NSURL.URLWithString(urlString) ?: return
        val isSameTrack = _currentTrack.value?.id == track.id
        
        _currentTrack.value = track
        savePlaybackState(track)
        _isLoading.value = true

        if (isSameTrack) {
            if (avPlayer.rate > 0) {
                avPlayer.pause()
                savePlaybackPosition(_currentPositionMs.value, _durationMs.value)
            } else {
                avPlayer.play()
            }
            _isPlaying.value = avPlayer.rate > 0
            return
        }

        _currentPositionMs.value = 0L
        _durationMs.value = 0L
        
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
            savePlaybackPosition(_currentPositionMs.value, _durationMs.value)
        } else {
            avPlayer.play()
        }
        _isPlaying.value = avPlayer.rate > 0
    }

    override fun seekTo(positionMs: Long) {
        val time = CMTimeMake(value = positionMs, timescale = 1000)
        avPlayer.seekToTime(time)
        _currentPositionMs.value = positionMs.coerceAtLeast(0L)
        savePlaybackPosition(_currentPositionMs.value, _durationMs.value)
    }

    private fun observeAppLifecycle() {
        val center = NSNotificationCenter.defaultCenter
        center.addObserverForName(UIApplicationDidEnterBackgroundNotification, null, null) { _ ->
            saveCurrentSnapshot()
        }
        center.addObserverForName(UIApplicationWillTerminateNotification, null, null) { _ ->
            saveCurrentSnapshot()
        }
    }

    private fun saveCurrentSnapshot() {
        val currentTimeSec = CMTimeGetSeconds(avPlayer.currentTime())
        val positionMs = if (currentTimeSec.isNaN()) _currentPositionMs.value else (currentTimeSec * 1000).toLong().coerceAtLeast(0L)
        savePlaybackPosition(positionMs, _durationMs.value)
    }

    private fun savePlaybackState(track: TrackUiModel) {
        prefs.setDouble(track.id.toDouble(), forKey = "last_track_id")
        prefs.setObject(track.title, forKey = "last_track_title")
        prefs.setObject(track.artist, forKey = "last_track_artist")
        prefs.setObject(track.audioUrl.orEmpty(), forKey = "last_track_url")
        prefs.setObject(track.artistImages.joinToString("|"), forKey = "last_track_images")
    }

    private fun savePlaybackPosition(posMs: Long, durationMs: Long) {
        if (_currentTrack.value == null) return
        prefs.setDouble(posMs.toDouble(), forKey = "last_position")
        if (durationMs > 0L) {
            prefs.setDouble(durationMs.toDouble(), forKey = "last_duration")
        }
    }

    private fun loadFromPrefs() {
        val trackId = prefs.doubleForKey("last_track_id").toLong()
        if (trackId <= 0L) return

        val title = prefs.stringForKey("last_track_title") ?: ""
        val artist = prefs.stringForKey("last_track_artist") ?: ""
        val url = prefs.stringForKey("last_track_url") ?: ""
        val images = (prefs.stringForKey("last_track_images") ?: "")
            .split("|")
            .filter { it.isNotBlank() }
        val positionMs = prefs.doubleForKey("last_position").toLong().coerceAtLeast(0L)
        val durationMs = prefs.doubleForKey("last_duration").toLong().coerceAtLeast(0L)

        if (title.isBlank() || url.isBlank()) return

        _currentTrack.value = TrackUiModel(
            id = trackId,
            title = title,
            artist = artist,
            audioUrl = url,
            coverUrl = images.firstOrNull(),
            artistImages = images,
        )
        _currentPositionMs.value = positionMs
        _durationMs.value = durationMs
        lastSavedPositionMs = positionMs
    }

    private fun restorePlayerFromSavedState() {
        val track = _currentTrack.value ?: return
        val url = NSURL.URLWithString(track.audioUrl ?: return) ?: return
        val positionMs = _currentPositionMs.value.coerceAtLeast(0L)
        val item = AVPlayerItem(uRL = url)
        avPlayer.replaceCurrentItemWithPlayerItem(item)
        if (positionMs > 0L) {
            avPlayer.seekToTime(CMTimeMake(value = positionMs, timescale = 1000))
        }
        avPlayer.pause()
        _isPlaying.value = false
    }
}
