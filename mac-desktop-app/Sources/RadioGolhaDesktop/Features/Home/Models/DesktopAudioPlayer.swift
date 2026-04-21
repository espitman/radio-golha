import Foundation
import AVFoundation
import AppKit

@MainActor
final class DesktopAudioPlayer: ObservableObject {
    @Published private(set) var currentTrack: TrackRowItem?
    @Published private(set) var isPlaying = false
    @Published private(set) var isLoading = false
    @Published private(set) var currentTime: Double = 0
    @Published private(set) var duration: Double = 0

    private var player: AVPlayer?
    private var timeObserverToken: Any?
    private var itemStatusObserver: NSKeyValueObservation?
    private var timeControlObserver: NSKeyValueObservation?
    private var lifecycleObservers: [NSObjectProtocol] = []
    private var pendingStartSeconds: Double?
    private var pendingAutoPlay = true
    private var lastPersistedAt = Date.distantPast

    private static let persistedStateKey = "radioGolha.desktop.audio.playbackState.v1"

    init() {
        registerLifecycleObservers()
        restorePersistedStateIfAvailable()
    }

    deinit {
        if let token = timeObserverToken {
            player?.removeTimeObserver(token)
            timeObserverToken = nil
        }
        lifecycleObservers.forEach(NotificationCenter.default.removeObserver)
        lifecycleObservers.removeAll()
    }

    func play(track: TrackRowItem) {
        startPlayback(track: track, startAtSeconds: nil, autoPlay: true)
    }

    private func startPlayback(track: TrackRowItem, startAtSeconds: Double?, autoPlay: Bool) {
        guard let urlString = track.audioURL, let url = URL(string: urlString) else {
            return
        }

        if currentTrack?.id == track.id {
            if let startAtSeconds {
                seekTo(seconds: startAtSeconds)
            }
            if autoPlay {
                player?.play()
                isPlaying = true
                isLoading = false
            } else {
                player?.pause()
                isPlaying = false
                isLoading = false
            }
            persistState(force: true)
            return
        }

        teardownObservers(resetTrack: false)
        currentTrack = track
        currentTime = 0
        duration = 0
        isLoading = true
        isPlaying = false
        pendingStartSeconds = startAtSeconds
        pendingAutoPlay = autoPlay

        let headers = [
            "Referer": "https://www.golha.co.uk/",
            "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X)"
        ]
        let asset = AVURLAsset(url: url, options: ["AVURLAssetHTTPHeaderFieldsKey": headers])
        let item = AVPlayerItem(asset: asset)
        let player = AVPlayer(playerItem: item)
        self.player = player
        bindObservers(to: item)

        if autoPlay {
            player.play()
            isPlaying = true
        }
        persistState(force: true)
    }

    func togglePlayPause() {
        guard player != nil else { return }
        guard !isLoading else { return }
        if isPlaying {
            player?.pause()
            isPlaying = false
        } else {
            player?.play()
            isPlaying = true
        }
        persistState(force: true)
    }

    var progress: Double {
        guard duration > 0 else { return 0 }
        return min(max(currentTime / duration, 0), 1)
    }

    func seek(toProgress progress: Double) {
        guard duration > 0 else { return }
        let clamped = min(max(progress, 0), 1)
        let seconds = duration * clamped
        seekTo(seconds: seconds)
        persistState(force: true)
    }

    private func bindObservers(to item: AVPlayerItem) {
        itemStatusObserver = item.observe(\.status, options: [.new, .initial]) { [weak self] item, _ in
            Task { @MainActor in
                guard let self else { return }
                if item.status == .readyToPlay {
                    let sec = item.duration.seconds
                    self.duration = sec.isFinite && sec > 0 ? sec : 0

                    if let startSeconds = self.pendingStartSeconds {
                        self.seekTo(seconds: startSeconds)
                    }
                    self.pendingStartSeconds = nil

                    if self.pendingAutoPlay {
                        self.player?.play()
                        self.isPlaying = true
                    } else {
                        self.player?.pause()
                        self.isPlaying = false
                        self.isLoading = false
                    }
                    self.persistState(force: true)
                } else if item.status == .failed {
                    self.isLoading = false
                    self.isPlaying = false
                    self.persistState(force: true)
                }
            }
        }

        if let player {
            timeControlObserver = player.observe(\.timeControlStatus, options: [.new, .initial]) { [weak self] player, _ in
                Task { @MainActor in
                    guard let self else { return }
                    switch player.timeControlStatus {
                    case .playing:
                        self.isLoading = false
                        self.isPlaying = true
                    case .waitingToPlayAtSpecifiedRate:
                        self.isLoading = true
                    case .paused:
                        if self.isLoading {
                            // Keep loading if item is still buffering.
                        } else {
                            self.isPlaying = false
                        }
                    @unknown default:
                        break
                    }
                }
            }

            let interval = CMTime(seconds: 0.25, preferredTimescale: 600)
            timeObserverToken = player.addPeriodicTimeObserver(forInterval: interval, queue: .main) { [weak self] time in
                guard self != nil else { return }
                let current = max(0, time.seconds)
                let sec = item.duration.seconds
                Task { @MainActor [weak self] in
                    guard let self else { return }
                    self.currentTime = current
                    if sec.isFinite && sec > 0 {
                        self.duration = sec
                    }
                    if current > 0, self.isLoading {
                        self.isLoading = false
                    }
                    self.persistState(force: false)
                }
            }
        }
    }

    private func teardownObservers(resetTrack: Bool = true) {
        if let token = timeObserverToken {
            player?.removeTimeObserver(token)
            timeObserverToken = nil
        }
        itemStatusObserver = nil
        timeControlObserver = nil
        pendingStartSeconds = nil
        pendingAutoPlay = true
        isLoading = false
        isPlaying = false
        currentTime = 0
        duration = 0
        if resetTrack {
            currentTrack = nil
            persistState(force: true)
        }
    }

    private func seekTo(seconds: Double) {
        let clamped = max(0, seconds)
        let target = CMTime(seconds: clamped, preferredTimescale: 600)
        player?.seek(to: target, toleranceBefore: .zero, toleranceAfter: .zero)
        currentTime = clamped
    }

    private func registerLifecycleObservers() {
        let center = NotificationCenter.default
        lifecycleObservers.append(
            center.addObserver(
                forName: NSApplication.willResignActiveNotification,
                object: nil,
                queue: .main
            ) { [weak self] _ in
                Task { @MainActor in
                    self?.persistState(force: true)
                }
            }
        )
        lifecycleObservers.append(
            center.addObserver(
                forName: NSApplication.willTerminateNotification,
                object: nil,
                queue: .main
            ) { [weak self] _ in
                Task { @MainActor in
                    self?.persistState(force: true)
                }
            }
        )
    }

    private func persistState(force: Bool) {
        guard let track = currentTrack else {
            UserDefaults.standard.removeObject(forKey: Self.persistedStateKey)
            return
        }

        let now = Date()
        if !force, now.timeIntervalSince(lastPersistedAt) < 1.0 {
            return
        }
        lastPersistedAt = now

        let state = PersistedPlaybackState(
            id: track.id,
            trackId: track.trackId,
            title: track.title,
            subtitle: track.subtitle,
            duration: track.duration,
            audioURL: track.audioURL,
            artworkURLs: track.artworkURLs,
            currentTime: currentTime,
            wasPlaying: isPlaying
        )

        guard let encoded = try? JSONEncoder().encode(state) else { return }
        UserDefaults.standard.set(encoded, forKey: Self.persistedStateKey)
    }

    private func restorePersistedStateIfAvailable() {
        guard let encoded = UserDefaults.standard.data(forKey: Self.persistedStateKey),
              let state = try? JSONDecoder().decode(PersistedPlaybackState.self, from: encoded),
              let audioURL = state.audioURL,
              !audioURL.isEmpty else {
            return
        }

        let track = TrackRowItem(
            id: state.id,
            trackId: state.trackId,
            title: state.title,
            subtitle: state.subtitle,
            duration: state.duration,
            audioURL: state.audioURL,
            artworkURLs: state.artworkURLs
        )

        startPlayback(
            track: track,
            startAtSeconds: max(0, state.currentTime),
            autoPlay: state.wasPlaying
        )
    }
}

private struct PersistedPlaybackState: Codable {
    let id: String
    let trackId: Int64?
    let title: String
    let subtitle: String
    let duration: String
    let audioURL: String?
    let artworkURLs: [String]
    let currentTime: Double
    let wasPlaying: Bool
}
