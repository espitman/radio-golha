import Foundation

struct ProgramItem: Identifiable {
    let id: String
    let sourceCategoryId: Int64?
    let title: String
    let count: String
    let symbol: String

    init(
        sourceCategoryId: Int64? = nil,
        title: String,
        count: String,
        symbol: String
    ) {
        self.sourceCategoryId = sourceCategoryId
        self.id = sourceCategoryId.map { "category-\($0)" } ?? UUID().uuidString
        self.title = title
        self.count = count
        self.symbol = symbol
    }
}

struct ArtistItem: Identifiable {
    let id = UUID()
    let sourceArtistId: Int64?
    let name: String
    let role: String
    let imageURL: String

    init(
        sourceArtistId: Int64? = nil,
        name: String,
        role: String,
        imageURL: String
    ) {
        self.sourceArtistId = sourceArtistId
        self.name = name
        self.role = role
        self.imageURL = imageURL
    }
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
    let artworkURLs: [String]

    init(
        id: String = UUID().uuidString,
        trackId: Int64? = nil,
        title: String,
        subtitle: String,
        duration: String,
        audioURL: String? = nil,
        artworkURLs: [String] = []
    ) {
        self.id = id
        self.trackId = trackId
        self.title = title
        self.subtitle = subtitle
        self.duration = duration
        self.audioURL = audioURL
        self.artworkURLs = artworkURLs
    }
}

struct SingerListItem: Identifiable {
    let id = UUID()
    let sourceArtistId: Int64?
    let name: String
    let programsCount: String
    let imageURL: String

    init(
        sourceArtistId: Int64? = nil,
        name: String,
        programsCount: String,
        imageURL: String
    ) {
        self.sourceArtistId = sourceArtistId
        self.name = name
        self.programsCount = programsCount
        self.imageURL = imageURL
    }
}

struct PlayerListItem: Identifiable {
    let id = UUID()
    let sourceArtistId: Int64?
    let name: String
    let instrument: String
    let programsCount: String
    let imageURL: String

    init(
        sourceArtistId: Int64? = nil,
        name: String,
        instrument: String,
        programsCount: String,
        imageURL: String
    ) {
        self.sourceArtistId = sourceArtistId
        self.name = name
        self.instrument = instrument
        self.programsCount = programsCount
        self.imageURL = imageURL
    }
}
