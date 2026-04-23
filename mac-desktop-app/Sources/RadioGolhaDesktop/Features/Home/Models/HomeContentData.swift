import Foundation

struct HomeContentData {
    let programs: [ProgramItem]
    let singers: [ArtistItem]
    let instrumentalists: [ArtistItem]
    let modes: [ModeItem]
    let duets: [DuetBannerItem]
    let topProgramsRows: [TrackRowItem]
    let latestTracksRows: [TrackRowItem]

    static let mock = HomeContentData(
        programs: HomeMockData.programs,
        singers: HomeMockData.singers,
        instrumentalists: HomeMockData.instrumentalists,
        modes: HomeMockData.modes,
        duets: HomeMockData.duets,
        topProgramsRows: HomeMockData.topProgramsRows,
        latestTracksRows: HomeMockData.latestTracksRows
    )

    func withTopTracks(topProgramsRows: [TrackRowItem], latestTracksRows: [TrackRowItem]) -> HomeContentData {
        HomeContentData(
            programs: programs,
            singers: singers,
            instrumentalists: instrumentalists,
            modes: modes,
            duets: duets,
            topProgramsRows: topProgramsRows,
            latestTracksRows: latestTracksRows
        )
    }
}
