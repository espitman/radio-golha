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
    @State private var artistDetailsLoadRequestId: String = ""
    @State private var homeContent: HomeContentData? = nil
    @State private var isHomeLoading = false
    @State private var isArtistDetailsLoading = false
    @State private var homeDataLoaded = false
    @StateObject private var audioPlayer = DesktopAudioPlayer()

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

            BottomPlayerSection(player: audioPlayer)
        }
        .environment(\.layoutDirection, .rightToLeft)
        .task {
            guard !homeDataLoaded else { return }
            homeDataLoaded = true
            isHomeLoading = true
            let start = Date()
            homeContent = await HomeDataLoader.load()
            let elapsed = Date().timeIntervalSince(start)
            let minVisible = 0.45
            if elapsed < minVisible {
                let remainingNs = UInt64((minVisible - elapsed) * 1_000_000_000)
                try? await Task.sleep(nanoseconds: remainingNs)
            }
            isHomeLoading = false
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
                    if let homeContent {
                        MainContentSection(
                            content: homeContent,
                            onRefreshTopTracks: {
                                guard let current = self.homeContent else { return }
                                if let rows = await HomeDataLoader.loadTopTracksRows() {
                                    self.homeContent = current.withTopTracks(
                                        topProgramsRows: rows.top,
                                        latestTracksRows: current.latestTracksRows
                                    )
                                }
                            },
                            onPlayTrack: { track in
                                if audioPlayer.currentTrack?.id == track.id {
                                    audioPlayer.togglePlayPause()
                                } else {
                                    audioPlayer.play(track: track)
                                }
                            },
                            currentPlayingTrackId: audioPlayer.currentTrack?.id,
                            isPlayerPlaying: audioPlayer.isPlaying,
                            isPlayerLoading: audioPlayer.isLoading
                        ) { artist in
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
                    } else if isHomeLoading {
                        DesktopLoadingView(message: "در حال بارگذاری صفحه اصلی...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else {
                        DesktopLoadingView(message: "در حال بارگذاری صفحه اصلی...")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
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
                    if isArtistDetailsLoading {
                        DesktopLoadingView(message: "در حال بارگذاری هنرمند...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else if let selectedArtistDetails {
                        ArtistDetailsContentView(
                            artist: selectedArtistDetails,
                            onBack: {
                                currentPage = artistDetailsSourcePage
                            },
                            onOpenArtist: { collaborator in
                                let targetDetails: ArtistDetailsItem = {
                                    if collaborator.sourceArtistId == nil {
                                        return ArtistDetailsFactory.fromArtistName(collaborator.name)
                                    }
                                    return ArtistDetailsFactory.fromCollaborator(collaborator)
                                }()
                                openArtistDetails(
                                    targetDetails,
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
                    } else {
                        DesktopLoadingView(message: "در حال بارگذاری هنرمند...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
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
        artistDetailsSourcePage = sourcePage
        currentPage = .artistDetails
        selectedArtistDetails = nil
        isArtistDetailsLoading = true
        guard let artistId = details.artistId else {
            selectedArtistDetails = details
            isArtistDetailsLoading = false
            return
        }
        let requestId = UUID().uuidString
        artistDetailsLoadRequestId = requestId

        Task {
            let start = Date()
            guard let loaded = await ArtistDetailsDataLoader.load(artistId: artistId, fallback: details) else {
                if artistDetailsLoadRequestId == requestId {
                    isArtistDetailsLoading = false
                }
                return
            }
            let elapsed = Date().timeIntervalSince(start)
            let minVisible = 0.35
            if elapsed < minVisible {
                let remainingNs = UInt64((minVisible - elapsed) * 1_000_000_000)
                try? await Task.sleep(nanoseconds: remainingNs)
            }
            guard artistDetailsLoadRequestId == requestId else { return }
            selectedArtistDetails = loaded
            isArtistDetailsLoading = false
        }
    }

    private func openProgramDetails(_ details: ProgramDetailsItem, sourcePage: DesktopPage) {
        selectedProgramDetails = details
        programDetailsSourcePage = sourcePage
        currentPage = .programDetails
    }
}

private struct DesktopLoadingView: View {
    let message: String

    var body: some View {
        ZStack {
            Palette.surface
            VStack(spacing: 12) {
                LoadingSpinner(color: Palette.secondary, size: 28, lineWidth: 3)
                Text(message)
                    .font(.vazir(11, .bold))
                    .foregroundStyle(Palette.primary)
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 16)
            .background(.white.opacity(0.92), in: RoundedRectangle(cornerRadius: 12, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .stroke(Palette.border, lineWidth: 1)
            )
        }
    }
}
