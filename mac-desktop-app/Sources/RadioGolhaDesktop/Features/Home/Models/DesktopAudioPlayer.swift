import Foundation
import AVFoundation

@MainActor
final class DesktopAudioPlayer: ObservableObject {
    @Published private(set) var currentTrack: TrackRowItem?
    @Published private(set) var isPlaying = false

    private var player: AVPlayer?

    func play(track: TrackRowItem) {
        guard let urlString = track.audioURL, let url = URL(string: urlString) else {
            return
        }

        if currentTrack?.id == track.id {
            player?.play()
            isPlaying = true
            return
        }

        currentTrack = track
        let item = AVPlayerItem(url: url)
        player = AVPlayer(playerItem: item)
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
}

