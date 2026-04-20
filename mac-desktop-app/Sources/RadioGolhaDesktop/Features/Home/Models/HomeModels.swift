import Foundation

struct ProgramItem: Identifiable {
    let id = UUID()
    let title: String
    let count: String
    let symbol: String
}

struct ArtistItem: Identifiable {
    let id = UUID()
    let name: String
    let role: String
    let imageURL: String
}

struct ModeItem: Identifiable {
    let id = UUID()
    let title: String
}

struct TrackRowItem: Identifiable {
    let id: String
    let trackId: Int64?
    let title: String
    let subtitle: String
    let duration: String
    let audioURL: String?
    let artworkURL: String?

    init(
        id: String = UUID().uuidString,
        trackId: Int64? = nil,
        title: String,
        subtitle: String,
        duration: String,
        audioURL: String? = nil,
        artworkURL: String? = nil
    ) {
        self.id = id
        self.trackId = trackId
        self.title = title
        self.subtitle = subtitle
        self.duration = duration
        self.audioURL = audioURL
        self.artworkURL = artworkURL
    }
}

struct SingerListItem: Identifiable {
    let id = UUID()
    let name: String
    let programsCount: String
    let imageURL: String
}

struct PlayerListItem: Identifiable {
    let id = UUID()
    let name: String
    let instrument: String
    let programsCount: String
    let imageURL: String
}
