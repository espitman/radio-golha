import Foundation

struct HomeContentData {
    let programs: [ProgramItem]
    let singers: [ArtistItem]
    let instrumentalists: [ArtistItem]
    let modes: [ModeItem]
    let topProgramsRows: [TrackRowItem]
    let latestTracksRows: [TrackRowItem]

    static let mock = HomeContentData(
        programs: HomeMockData.programs,
        singers: HomeMockData.singers,
        instrumentalists: HomeMockData.instrumentalists,
        modes: HomeMockData.modes,
        topProgramsRows: HomeMockData.topProgramsRows,
        latestTracksRows: HomeMockData.latestTracksRows
    )
}

