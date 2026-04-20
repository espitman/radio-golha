import SwiftUI

private enum DesktopPage {
    case home
    case singers
    case players
    case artistDetails
    case programDetails
}

struct HomeRootView: View {
    @State private var currentPage: DesktopPage = .home
    @State private var selectedArtistDetails: ArtistDetailsItem? = nil
    @State private var artistDetailsSourcePage: DesktopPage = .home
    @State private var selectedProgramDetails: ProgramDetailsItem? = nil
    @State private var programDetailsSourcePage: DesktopPage = .home
    @State private var homeContent: HomeContentData = .mock
    @State private var homeDataLoaded = false

    var body: some View {
        ZStack(alignment: .bottom) {
            Palette.surface.ignoresSafeArea()

            HStack(spacing: 0) {
                mainCanvas
                    .frame(width: 1024)
                    .environment(\.layoutDirection, .rightToLeft)
                SidebarSection()
                    .frame(width: 256)
                    .environment(\.layoutDirection, .rightToLeft)
            }
            .frame(width: 1280)
            .environment(\.layoutDirection, .leftToRight)

            BottomPlayerSection()
        }
        .environment(\.layoutDirection, .rightToLeft)
        .task {
            guard !homeDataLoaded else { return }
            homeDataLoaded = true
            homeContent = await HomeDataLoader.load()
        }
    }

    private var mainCanvas: some View {
        VStack(spacing: 0) {
            DesktopTopNavigationBar(
                selectedTab: selectedTab
            ) { tab in
                switch tab {
                case .artists:
                    currentPage = .singers
                case .instrumentalists:
                    currentPage = .players
                case .programs:
                    currentPage = .home
                default:
                    break
                }
            }

            Group {
                switch currentPage {
                case .home:
                    MainContentSection(content: homeContent) { artist in
                        openArtistDetails(
                            ArtistDetailsFactory.fromHomeArtist(artist),
                            sourcePage: .home
                        )
                    } onProgramTap: { trackTitle in
                        openProgramDetails(
                            ProgramDetailsFactory.fromTrackTitle(trackTitle),
                            sourcePage: .home
                        )
                    }
                case .singers:
                    SingersContentView { singer in
                        openArtistDetails(
                            ArtistDetailsFactory.fromSinger(singer),
                            sourcePage: .singers
                        )
                    }
                case .players:
                    PlayersContentView { player in
                        openArtistDetails(
                            ArtistDetailsFactory.fromPlayer(player),
                            sourcePage: .players
                        )
                    }
                case .artistDetails:
                    if let selectedArtistDetails {
                        ArtistDetailsContentView(
                            artist: selectedArtistDetails,
                            onBack: {
                                currentPage = artistDetailsSourcePage
                            },
                            onOpenArtist: { artistName in
                                openArtistDetails(
                                    ArtistDetailsFactory.fromArtistName(artistName),
                                    sourcePage: artistDetailsSourcePage
                                )
                            },
                            onOpenProgram: { trackTitle in
                                openProgramDetails(
                                    ProgramDetailsFactory.fromTrackTitle(trackTitle),
                                    sourcePage: .artistDetails
                                )
                            }
                        )
                    }
                case .programDetails:
                    if let selectedProgramDetails {
                        ProgramDetailsContentView(
                            program: selectedProgramDetails,
                            onBack: {
                                currentPage = programDetailsSourcePage
                            }
                        )
                    }
                }
            }
            .frame(maxHeight: .infinity)
        }
    }

    private var selectedTab: DesktopMainTab {
        switch currentPage {
        case .home:
            return .programs
        case .singers:
            return .artists
        case .players:
            return .instrumentalists
        case .artistDetails:
            switch artistDetailsSourcePage {
            case .singers:
                return .artists
            case .players:
                return .instrumentalists
            default:
                return .programs
            }
        case .programDetails:
            switch programDetailsSourcePage {
            case .singers:
                return .artists
            case .players:
                return .instrumentalists
            default:
                return .programs
            }
        }
    }

    private func openArtistDetails(_ details: ArtistDetailsItem, sourcePage: DesktopPage) {
        selectedArtistDetails = details
        artistDetailsSourcePage = sourcePage
        currentPage = .artistDetails
    }

    private func openProgramDetails(_ details: ProgramDetailsItem, sourcePage: DesktopPage) {
        selectedProgramDetails = details
        programDetailsSourcePage = sourcePage
        currentPage = .programDetails
    }
}
