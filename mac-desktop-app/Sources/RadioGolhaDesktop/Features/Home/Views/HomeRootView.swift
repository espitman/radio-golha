import SwiftUI

private enum DesktopPage {
    case home
    case singers
    case players
    case programTracks
    case artistDetails
    case programDetails
}

private struct NavigationSnapshot {
    let page: DesktopPage
    let selectedArtistDetails: ArtistDetailsItem?
    let artistDetailsSourcePage: DesktopPage
    let selectedProgramDetails: ProgramDetailsItem?
    let programDetailsSourcePage: DesktopPage
    let selectedProgramCategory: ProgramItem?
    let selectedProgramTracks: [TrackRowItem]?
    let isProgramTracksLoading: Bool
    let isArtistDetailsLoading: Bool
    let isProgramDetailsLoading: Bool
}

struct HomeRootView: View {
    @State private var currentPage: DesktopPage = .home
    @State private var selectedArtistDetails: ArtistDetailsItem? = nil
    @State private var artistDetailsSourcePage: DesktopPage = .home
    @State private var selectedProgramDetails: ProgramDetailsItem? = nil
    @State private var programDetailsSourcePage: DesktopPage = .home
    @State private var selectedProgramCategory: ProgramItem? = nil
    @State private var selectedProgramTracks: [TrackRowItem]? = nil
    @State private var artistDetailsLoadRequestId: String = ""
    @State private var homeContent: HomeContentData? = nil
    @State private var singersContent: [SingerListItem]? = nil
    @State private var playersContent: [PlayerListItem]? = nil
    @State private var isHomeLoading = false
    @State private var isSingersLoading = false
    @State private var isPlayersLoading = false
    @State private var isProgramTracksLoading = false
    @State private var isArtistDetailsLoading = false
    @State private var isProgramDetailsLoading = false
    @State private var homeDataLoaded = false
    @State private var singersDataLoaded = false
    @State private var playersDataLoaded = false
    @State private var backStack: [NavigationSnapshot] = []
    @State private var programDetailsLoadRequestId: String = ""
    @StateObject private var audioPlayer = DesktopAudioPlayer()
    private let pageTransition = AnyTransition.asymmetric(
        insertion: .offset(y: -10).combined(with: .opacity),
        removal: .offset(y: -8).combined(with: .opacity)
    )
    private let pageAnimation = Animation.easeInOut(duration: 0.22)

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
            let loaded = await HomeDataLoader.load()
            let elapsed = Date().timeIntervalSince(start)
            let minVisible = 0.45
            if elapsed < minVisible {
                let remainingNs = UInt64((minVisible - elapsed) * 1_000_000_000)
                try? await Task.sleep(nanoseconds: remainingNs)
            }
            withAnimation(pageAnimation) {
                homeContent = loaded
                isHomeLoading = false
            }
        }
    }

    private var mainCanvas: some View {
        VStack(spacing: 0) {
            DesktopTopNavigationBar(
                selectedTab: selectedTab,
                canGoBack: canGoBack,
                onBack: handleBack
            ) { tab in
                switch tab {
                case .artists:
                    navigateTo(.singers)
                    ensureSingersLoaded()
                case .instrumentalists:
                    navigateTo(.players)
                    ensurePlayersLoaded()
                case .programs:
                    navigateTo(.home)
                default:
                    break
                }
            }

            ZStack {
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
                                handleTrackPlayIntent(track)
                            },
                            currentPlayingTrackId: audioPlayer.currentTrack?.id,
                            isPlayerPlaying: audioPlayer.isPlaying,
                            isPlayerLoading: audioPlayer.isLoading,
                            onArtistTap: { artist in
                                openArtistDetails(
                                    ArtistDetailsFactory.fromHomeArtist(artist),
                                    sourcePage: .home
                                )
                            },
                            onProgramCategoryTap: { category in
                                openProgramTracks(category)
                            },
                            onProgramTap: { row in
                                openProgramDetails(
                                    programId: row.trackId,
                                    fallbackTitle: row.title,
                                    sourcePage: .home
                                )
                            },
                            onShowAllSingers: {
                                navigateTo(.singers)
                                ensureSingersLoaded()
                            },
                            onShowAllInstrumentalists: {
                                navigateTo(.players)
                                ensurePlayersLoaded()
                            }
                        )
                        .transition(pageTransition)
                    } else if isHomeLoading {
                        DesktopLoadingView(message: "در حال بارگذاری صفحه اصلی...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .transition(pageTransition)
                    } else {
                        DesktopLoadingView(message: "در حال بارگذاری صفحه اصلی...")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .transition(pageTransition)
                    }
                case .singers:
                    if let singersContent {
                        SingersContentView(singers: singersContent) { singer in
                            openArtistDetails(
                                ArtistDetailsFactory.fromSinger(singer),
                                sourcePage: .singers
                            )
                        }
                        .transition(pageTransition)
                    } else {
                        DesktopLoadingView(message: "در حال بارگذاری خواننده‌ها...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .transition(pageTransition)
                    }
                case .players:
                    if let playersContent {
                        PlayersContentView(players: playersContent) { player in
                            openArtistDetails(
                                ArtistDetailsFactory.fromPlayer(player),
                                sourcePage: .players
                            )
                        }
                        .transition(pageTransition)
                    } else {
                        DesktopLoadingView(message: "در حال بارگذاری نوازندگان...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .transition(pageTransition)
                    }
                case .programTracks:
                    if let category = selectedProgramCategory {
                        if let selectedProgramTracks {
                            ProgramTracksContentView(
                                category: category,
                                tracks: selectedProgramTracks,
                                onPlayTrack: { track in
                                    handleTrackPlayIntent(track)
                                },
                                onOpenProgram: { track in
                                    openProgramDetails(
                                        programId: track.trackId,
                                        fallbackTitle: track.title,
                                        sourcePage: .programTracks
                                    )
                                },
                                currentPlayingTrackId: audioPlayer.currentTrack?.id,
                                isPlayerPlaying: audioPlayer.isPlaying,
                                isPlayerLoading: audioPlayer.isLoading
                            )
                            .transition(pageTransition)
                        } else {
                            DesktopLoadingView(message: isProgramTracksLoading ? "در حال بارگذاری قطعات برنامه..." : "قطعه‌ای برای این برنامه پیدا نشد")
                                .frame(maxWidth: .infinity, maxHeight: .infinity)
                                .transition(pageTransition)
                        }
                    } else {
                        DesktopLoadingView(message: "در حال بارگذاری برنامه...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .transition(pageTransition)
                    }
                case .artistDetails:
                    if isArtistDetailsLoading {
                        DesktopLoadingView(message: "در حال بارگذاری هنرمند...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .transition(pageTransition)
                    } else if let selectedArtistDetails {
                        ArtistDetailsContentView(
                            artist: selectedArtistDetails,
                            onBack: {
                                handleBack()
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
                            onOpenProgram: { row in
                                openProgramDetails(
                                    programId: row.programId ?? row.trackId,
                                    fallbackTitle: row.title,
                                    sourcePage: .artistDetails
                                )
                            },
                            onPlayTrack: { row in
                                let id: String = {
                                    if let trackId = row.trackId {
                                        return "track-\(trackId)"
                                    }
                                    return "artist-\(selectedArtistDetails.id)-\(row.title)-\(row.subtitle)-\(row.duration)"
                                }()
                                let track = TrackRowItem(
                                    id: id,
                                    trackId: row.trackId,
                                    title: row.title,
                                    subtitle: row.subtitle,
                                    duration: row.duration,
                                    audioURL: row.audioURL,
                                    artworkURLs: [selectedArtistDetails.imageURL]
                                )
                                handleTrackPlayIntent(track)
                            },
                            currentPlayingTrackId: audioPlayer.currentTrack?.id,
                            isPlayerPlaying: audioPlayer.isPlaying,
                            isPlayerLoading: audioPlayer.isLoading
                        )
                        .transition(pageTransition)
                    } else {
                        DesktopLoadingView(message: "در حال بارگذاری هنرمند...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .transition(pageTransition)
                    }
                case .programDetails:
                    if isProgramDetailsLoading {
                        DesktopLoadingView(message: "در حال بارگذاری برنامه...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .transition(pageTransition)
                    } else if let selectedProgramDetails {
                        ProgramDetailsContentView(
                            program: selectedProgramDetails,
                            player: audioPlayer,
                            onBack: {
                                handleBack()
                            }
                        )
                        .transition(pageTransition)
                    } else {
                        DesktopLoadingView(message: "در حال بارگذاری برنامه...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .transition(pageTransition)
                    }
                }
            }
            .frame(maxHeight: .infinity)
            .animation(pageAnimation, value: currentPage)
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
        case .programTracks:
            return .programs
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

    private var canGoBack: Bool {
        !backStack.isEmpty
    }

    private func makeSnapshot() -> NavigationSnapshot {
        NavigationSnapshot(
            page: currentPage,
            selectedArtistDetails: selectedArtistDetails,
            artistDetailsSourcePage: artistDetailsSourcePage,
            selectedProgramDetails: selectedProgramDetails,
            programDetailsSourcePage: programDetailsSourcePage,
            selectedProgramCategory: selectedProgramCategory,
            selectedProgramTracks: selectedProgramTracks,
            isProgramTracksLoading: isProgramTracksLoading,
            isArtistDetailsLoading: isArtistDetailsLoading,
            isProgramDetailsLoading: isProgramDetailsLoading
        )
    }

    private func restore(from snapshot: NavigationSnapshot) {
        withAnimation(pageAnimation) {
            currentPage = snapshot.page
            selectedArtistDetails = snapshot.selectedArtistDetails
            artistDetailsSourcePage = snapshot.artistDetailsSourcePage
            selectedProgramDetails = snapshot.selectedProgramDetails
            programDetailsSourcePage = snapshot.programDetailsSourcePage
            selectedProgramCategory = snapshot.selectedProgramCategory
            selectedProgramTracks = snapshot.selectedProgramTracks
            isProgramTracksLoading = snapshot.isProgramTracksLoading
            isArtistDetailsLoading = snapshot.isArtistDetailsLoading
            isProgramDetailsLoading = snapshot.isProgramDetailsLoading
        }
    }

    private func navigateTo(_ page: DesktopPage) {
        guard currentPage != page else { return }
        backStack.append(makeSnapshot())
        withAnimation(pageAnimation) {
            currentPage = page
        }
    }

    private func handleBack() {
        guard let snapshot = backStack.popLast() else { return }
        artistDetailsLoadRequestId = UUID().uuidString
        programDetailsLoadRequestId = UUID().uuidString
        restore(from: snapshot)
    }

    private func ensureSingersLoaded() {
        guard !singersDataLoaded && !isSingersLoading else { return }
        singersDataLoaded = true
        isSingersLoading = true

        Task {
            let start = Date()
            let loaded = await SingersDataLoader.load()
            let elapsed = Date().timeIntervalSince(start)
            let minVisible = 0.35
            if elapsed < minVisible {
                let remainingNs = UInt64((minVisible - elapsed) * 1_000_000_000)
                try? await Task.sleep(nanoseconds: remainingNs)
            }
            withAnimation(pageAnimation) {
                singersContent = loaded
                isSingersLoading = false
            }
        }
    }

    private func ensurePlayersLoaded() {
        guard !playersDataLoaded && !isPlayersLoading else { return }
        playersDataLoaded = true
        isPlayersLoading = true

        Task {
            let start = Date()
            let loaded = await PlayersDataLoader.load()
            let elapsed = Date().timeIntervalSince(start)
            let minVisible = 0.35
            if elapsed < minVisible {
                let remainingNs = UInt64((minVisible - elapsed) * 1_000_000_000)
                try? await Task.sleep(nanoseconds: remainingNs)
            }
            withAnimation(pageAnimation) {
                playersContent = loaded
                isPlayersLoading = false
            }
        }
    }

    private func openArtistDetails(_ details: ArtistDetailsItem, sourcePage: DesktopPage) {
        backStack.append(makeSnapshot())
        artistDetailsSourcePage = sourcePage
        withAnimation(pageAnimation) {
            currentPage = .artistDetails
        }
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
                    withAnimation(pageAnimation) {
                        isArtistDetailsLoading = false
                    }
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
            withAnimation(pageAnimation) {
                selectedArtistDetails = loaded
                isArtistDetailsLoading = false
            }
        }
    }

    private func openProgramDetails(programId: Int64?, fallbackTitle: String, sourcePage: DesktopPage) {
        backStack.append(makeSnapshot())
        programDetailsSourcePage = sourcePage
        withAnimation(pageAnimation) {
            currentPage = .programDetails
        }

        selectedProgramDetails = nil
        isProgramDetailsLoading = true

        guard let programId else {
            withAnimation(pageAnimation) {
                selectedProgramDetails = ProgramDetailsFactory.fromTrackTitle(fallbackTitle)
                isProgramDetailsLoading = false
            }
            return
        }

        let requestId = UUID().uuidString
        programDetailsLoadRequestId = requestId

        Task {
            let start = Date()
            let loaded = await ProgramDetailsDataLoader.load(programId: programId, fallbackTitle: fallbackTitle)
                ?? ProgramDetailsFactory.fromTrackTitle(fallbackTitle)
            let elapsed = Date().timeIntervalSince(start)
            let minVisible = 0.3
            if elapsed < minVisible {
                let remainingNs = UInt64((minVisible - elapsed) * 1_000_000_000)
                try? await Task.sleep(nanoseconds: remainingNs)
            }
            guard programDetailsLoadRequestId == requestId else { return }
            withAnimation(pageAnimation) {
                selectedProgramDetails = loaded
                isProgramDetailsLoading = false
            }
        }
    }

    private func handleTrackPlayIntent(_ track: TrackRowItem) {
        if audioPlayer.currentTrack?.id == track.id {
            if audioPlayer.isPlaying {
                audioPlayer.togglePlayPause()
            } else {
                audioPlayer.togglePlayPause()
                pushRecentTrackToTop(track)
            }
            return
        }

        audioPlayer.play(track: track)
        pushRecentTrackToTop(track)
    }

    private func pushRecentTrackToTop(_ track: TrackRowItem) {
        guard var content = homeContent else { return }

        let deduped = content.latestTracksRows.filter { existing in
            if existing.id == track.id { return false }
            if let lhs = existing.trackId, let rhs = track.trackId { return lhs != rhs }
            return true
        }

        content = content.withTopTracks(
            topProgramsRows: content.topProgramsRows,
            latestTracksRows: Array(([track] + deduped).prefix(5))
        )
        homeContent = content
    }

    private func openProgramTracks(_ category: ProgramItem) {
        backStack.append(makeSnapshot())
        selectedProgramCategory = category
        selectedProgramTracks = nil
        isProgramTracksLoading = true
        withAnimation(pageAnimation) {
            currentPage = .programTracks
        }

        guard let categoryId = category.sourceCategoryId else {
            isProgramTracksLoading = false
            selectedProgramTracks = []
            return
        }

        Task {
            let start = Date()
            let loaded = await ProgramTracksDataLoader.load(categoryId: categoryId) ?? []
            let elapsed = Date().timeIntervalSince(start)
            let minVisible = 0.3
            if elapsed < minVisible {
                let remainingNs = UInt64((minVisible - elapsed) * 1_000_000_000)
                try? await Task.sleep(nanoseconds: remainingNs)
            }

            guard selectedProgramCategory?.id == category.id, currentPage == .programTracks else { return }
            withAnimation(pageAnimation) {
                selectedProgramTracks = loaded
                isProgramTracksLoading = false
            }
        }
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
