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
    let id = UUID()
    let title: String
    let subtitle: String
    let duration: String
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
