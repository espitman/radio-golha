import Foundation
import AVFoundation

@MainActor
final class DesktopAudioPlayer: ObservableObject {
    @Published private(set) var currentTrack: TrackRowItem?
    @Published private(set) var isPlaying = false
    @Published private(set) var currentTime: Double = 0
    @Published private(set) var duration: Double = 0

    private var player: AVPlayer?
    private var timeObserverToken: Any?
    private var itemStatusObserver: NSKeyValueObservation?

    func play(track: TrackRowItem) {
        guard let urlString = track.audioURL, let url = URL(string: urlString) else {
            return
        }

        if currentTrack?.id == track.id {
            player?.play()
            isPlaying = true
            return
        }

        teardownObservers()
        currentTrack = track
        let item = AVPlayerItem(url: url)
        player = AVPlayer(playerItem: item)
        bindObservers(to: item)
        player?.play()
        isPlaying = true
    }

    func togglePlayPause() {
        guard player != nil else { return }
        if isPlaying {
            player?.pause()
            isPlaying = false
        } else {
            player?.play()
            isPlaying = true
        }
    }

    var progress: Double {
        guard duration > 0 else { return 0 }
        return min(max(currentTime / duration, 0), 1)
    }

    func seek(toProgress progress: Double) {
        guard duration > 0 else { return }
        let clamped = min(max(progress, 0), 1)
        let seconds = duration * clamped
        let target = CMTime(seconds: seconds, preferredTimescale: 600)
        player?.seek(to: target, toleranceBefore: .zero, toleranceAfter: .zero)
        currentTime = seconds
    }

    private func bindObservers(to item: AVPlayerItem) {
        itemStatusObserver = item.observe(\.status, options: [.new, .initial]) { [weak self] item, _ in
            Task { @MainActor in
                guard let self else { return }
                if item.status == .readyToPlay {
                    let sec = item.duration.seconds
                    self.duration = sec.isFinite && sec > 0 ? sec : 0
                }
            }
        }

        if let player {
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
                }
            }
        }
    }

    private func teardownObservers() {
        if let token = timeObserverToken {
            player?.removeTimeObserver(token)
            timeObserverToken = nil
        }
        itemStatusObserver = nil
        currentTime = 0
        duration = 0
    }
}
