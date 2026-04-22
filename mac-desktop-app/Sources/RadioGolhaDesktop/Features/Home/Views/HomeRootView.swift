import SwiftUI
import AppKit

private enum DesktopPage {
    case home
    case search
    case searchResults
    case singers
    case favoriteSingers
    case players
    case favoritePlayers
    case playlists
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
    let selectedProgramTracksBadge: String
    let selectedProgramTracksDuet: DuetBannerItem?
    let selectedProgramTracksSidebarItem: SidebarMenuItem?
    let selectedProgramTracks: [TrackRowItem]?
    let selectedSearchTracks: [TrackRowItem]?
    let selectedSearchTotal: Int
    let activeSearchFilters: SearchProgramFilters
    let activeSearchChips: [SearchActiveChip]
    let isProgramTracksLoading: Bool
    let isSearchResultsLoading: Bool
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
    @State private var selectedProgramTracksBadge: String = ""
    @State private var selectedProgramTracksDuet: DuetBannerItem? = nil
    @State private var selectedProgramTracksSidebarItem: SidebarMenuItem? = nil
    @State private var selectedProgramTracks: [TrackRowItem]? = nil
    @State private var selectedSearchTracks: [TrackRowItem]? = nil
    @State private var selectedSearchTotal: Int = 0
    @State private var activeSearchFilters: SearchProgramFilters = .empty
    @State private var activeSearchChips: [SearchActiveChip] = []
    @State private var artistDetailsLoadRequestId: String = ""
    @State private var homeContent: HomeContentData? = nil
    @State private var singersContent: [SingerListItem]? = nil
    @State private var playersContent: [PlayerListItem]? = nil
    @State private var isHomeLoading = false
    @State private var isSingersLoading = false
    @State private var isPlayersLoading = false
    @State private var isProgramTracksLoading = false
    @State private var isSearchResultsLoading = false
    @State private var isArtistDetailsLoading = false
    @State private var isProgramDetailsLoading = false
    @State private var homeDataLoaded = false
    @State private var singersDataLoaded = false
    @State private var playersDataLoaded = false
    @State private var backStack: [NavigationSnapshot] = []
    @State private var programDetailsLoadRequestId: String = ""
    @State private var programTracksLoadRequestId: String = ""
    @State private var manualPlaylists: [DesktopManualPlaylist] = []
    @State private var favoriteArtistIds: Set<Int64> = []
    @State private var pendingPlaylistTrackId: Int64? = nil
    @State private var pendingPlaylistTrackIds: [Int64]? = nil
    @State private var isCreatingPlaylistOnly: Bool = false
    @State private var pendingPlaylistName: String = ""
    @State private var canDismissPlaylistDialogByBackdrop: Bool = false
    @State private var isPlaylistNameFieldFocused: Bool = false
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
                SidebarSection(
                    selectedItem: selectedSidebarItem,
                    onSelectItem: { item in
                        switch item {
                        case .favoriteSingers:
                            navigateTo(.favoriteSingers)
                            ensureSingersLoaded()
                        case .favoritePlayers:
                            navigateTo(.favoritePlayers)
                            ensurePlayersLoaded()
                        case .myPlaylists:
                            navigateTo(.playlists)
                        case .topPrograms:
                            openMostPlayedTracksCollection()
                        case .recentlyPlayed:
                            openRecentlyPlayedTracksCollection()
                        }
                    }
                )
                    .frame(width: 256)
                    .environment(\.layoutDirection, .rightToLeft)
            }
            .frame(width: 1280)
            .environment(\.layoutDirection, .leftToRight)

            if pendingPlaylistTrackId != nil || pendingPlaylistTrackIds != nil || isCreatingPlaylistOnly {
                Color.black.opacity(0.22)
                    .ignoresSafeArea()
                    .zIndex(2_000)
                    .transition(.opacity)
                    .onTapGesture {
                        guard canDismissPlaylistDialogByBackdrop else { return }
                        dismissPlaylistDialog()
                    }

                playlistCreateDialog
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
                    .transition(.opacity.combined(with: .scale(scale: 0.96)))
                    .zIndex(2_001)
            }

            BottomPlayerSection(
                player: audioPlayer,
                onOpenCurrentTrack: { track in
                    openProgramDetails(
                        programId: track.trackId,
                        fallbackTitle: track.title,
                        sourcePage: currentPage
                    )
                }
            )
        }
        .environment(\.layoutDirection, .rightToLeft)
        .animation(.easeInOut(duration: 0.2), value: pendingPlaylistTrackId != nil)
        .task {
            guard !homeDataLoaded else { return }
            homeDataLoaded = true
            isHomeLoading = true
            manualPlaylists = await DesktopPlaylistDataLoader.loadManualPlaylists()
            favoriteArtistIds = await DesktopFavoriteArtistsDataLoader.loadFavoriteArtistIds()
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
                onBack: handleBack,
                onSelectTab: { tab in
                    switch tab {
                    case .artists:
                        navigateTo(.singers)
                        ensureSingersLoaded()
                    case .search:
                        navigateTo(.search)
                    case .instrumentalists:
                        navigateTo(.players)
                        ensurePlayersLoaded()
                    case .programs:
                        navigateTo(.home)
                    default:
                        break
                    }
                },
                onOpenQuickArtist: { artistId, name, avatar in
                    openArtistDetails(
                        ArtistDetailsFactory.fromHomeArtist(
                            ArtistItem(
                                sourceArtistId: artistId,
                                name: name,
                                role: "هنرمند",
                                imageURL: avatar ?? ""
                            )
                        ),
                        sourcePage: currentPage
                    )
                },
                onOpenQuickTrack: { trackId, title in
                    openProgramDetails(
                        programId: trackId,
                        fallbackTitle: title,
                        sourcePage: currentPage
                    )
                }
            )

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
                            onDuetTap: { duet in
                                openDuetTracksCollection(duet)
                            },
                            manualPlaylists: manualPlaylists,
                            onAddTrackToPlaylist: { playlistId, trackId in
                                Task {
                                    let _ = await DesktopPlaylistDataLoader.addTrack(playlistId: playlistId, trackId: trackId)
                                    await refreshManualPlaylists()
                                }
                            },
                            onRemoveTrackFromPlaylist: { playlistId, trackId in
                                Task {
                                    let _ = await DesktopPlaylistDataLoader.removeTrack(playlistId: playlistId, trackId: trackId)
                                    await refreshManualPlaylists()
                                }
                            },
                            onCreatePlaylistAndAddTrack: { trackId in
                                createPlaylistAndAddTrack(trackId)
                            },
                            onShowAllSingers: {
                                navigateTo(.singers)
                                ensureSingersLoaded()
                            },
                            onShowAllInstrumentalists: {
                                navigateTo(.players)
                                ensurePlayersLoaded()
                            },
                            favoriteArtistIds: favoriteArtistIds,
                            onToggleArtistFavorite: toggleArtistFavorite
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
                        SingersContentView(
                            singers: singersContent,
                            onSingerTap: { singer in
                                openArtistDetails(
                                    ArtistDetailsFactory.fromSinger(singer),
                                    sourcePage: .singers
                                )
                            },
                            favoriteArtistIds: favoriteArtistIds,
                            onToggleArtistFavorite: toggleArtistFavorite
                        )
                        .transition(pageTransition)
                    } else {
                        DesktopLoadingView(message: "در حال بارگذاری خواننده‌ها...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .transition(pageTransition)
                    }
                case .favoriteSingers:
                    if let singersContent {
                        SingersContentView(
                            singers: singersContent.filter { item in
                                guard let id = item.sourceArtistId else { return false }
                                return favoriteArtistIds.contains(id)
                            },
                            title: "خواننده‌های مورد علاقه",
                            subtitle: "فهرست خواننده‌هایی که به علاقه‌مندی‌های شما اضافه شده‌اند.",
                            showAlphabetFilter: false,
                            showHeroBanner: true,
                            heroBadge: "مجموعه ویژه",
                            onSingerTap: { singer in
                                openArtistDetails(
                                    ArtistDetailsFactory.fromSinger(singer),
                                    sourcePage: .favoriteSingers
                                )
                            },
                            favoriteArtistIds: favoriteArtistIds,
                            onToggleArtistFavorite: toggleArtistFavorite
                        )
                        .transition(pageTransition)
                    } else {
                        DesktopLoadingView(message: "در حال بارگذاری خواننده‌های مورد علاقه...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .transition(pageTransition)
                    }
                case .search:
                    SearchContentView(
                        initialFilters: activeSearchFilters,
                        onSearch: { filters, chips in
                            openSearchResults(filters, chips: chips)
                        }
                    )
                    .id("search-\(activeSearchFilters.restoreKey)")
                        .transition(pageTransition)
                case .searchResults:
                    if isSearchResultsLoading {
                        DesktopLoadingView(message: "در حال جستجو...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .transition(pageTransition)
                    } else if let selectedSearchTracks {
                        SearchResultsContentView(
                            tracks: selectedSearchTracks,
                            total: selectedSearchTotal,
                            activeChips: activeSearchChips,
                            onRemoveChip: { chip in
                                removeSearchChip(chip)
                            },
                            onPlayTrack: { track in
                                handleTrackPlayIntent(track)
                            },
                            onOpenProgram: { track in
                                openProgramDetails(
                                    programId: track.trackId,
                                    fallbackTitle: track.title,
                                    sourcePage: .searchResults
                                )
                            },
                            onSaveAsPlaylist: {
                                let trackIds = selectedSearchTracks.compactMap(\.trackId)
                                guard !trackIds.isEmpty else { return }
                                createPlaylistAndAddTracks(trackIds)
                            },
                            manualPlaylists: manualPlaylists,
                            onAddTrackToPlaylist: { playlistId, trackId in
                                Task {
                                    let _ = await DesktopPlaylistDataLoader.addTrack(playlistId: playlistId, trackId: trackId)
                                    await refreshManualPlaylists()
                                }
                            },
                            onRemoveTrackFromPlaylist: { playlistId, trackId in
                                Task {
                                    let _ = await DesktopPlaylistDataLoader.removeTrack(playlistId: playlistId, trackId: trackId)
                                    await refreshManualPlaylists()
                                }
                            },
                            onCreatePlaylistAndAddTrack: { trackId in
                                createPlaylistAndAddTrack(trackId)
                            },
                            currentPlayingTrackId: audioPlayer.currentTrack?.id,
                            isPlayerPlaying: audioPlayer.isPlaying,
                            isPlayerLoading: audioPlayer.isLoading
                        )
                        .transition(pageTransition)
                    } else {
                        DesktopLoadingView(message: "نتیجه‌ای یافت نشد")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .transition(pageTransition)
                    }
                case .players:
                    if let playersContent {
                        PlayersContentView(
                            players: playersContent,
                            onPlayerTap: { player in
                                openArtistDetails(
                                    ArtistDetailsFactory.fromPlayer(player),
                                    sourcePage: .players
                                )
                            },
                            favoriteArtistIds: favoriteArtistIds,
                            onToggleArtistFavorite: toggleArtistFavorite
                        )
                        .transition(pageTransition)
                    } else {
                        DesktopLoadingView(message: "در حال بارگذاری نوازندگان...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .transition(pageTransition)
                    }
                case .favoritePlayers:
                    if let playersContent {
                        PlayersContentView(
                            players: playersContent.filter { item in
                                guard let id = item.sourceArtistId else { return false }
                                return favoriteArtistIds.contains(id)
                            },
                            title: "نوازندگان مورد علاقه",
                            subtitle: "فهرست نوازنده‌هایی که به علاقه‌مندی‌های شما اضافه شده‌اند.",
                            showInstrumentFilter: false,
                            showHeroBanner: true,
                            heroBadge: "مجموعه ویژه",
                            onPlayerTap: { player in
                                openArtistDetails(
                                    ArtistDetailsFactory.fromPlayer(player),
                                    sourcePage: .favoritePlayers
                                )
                            },
                            favoriteArtistIds: favoriteArtistIds,
                            onToggleArtistFavorite: toggleArtistFavorite
                        )
                        .transition(pageTransition)
                    } else {
                        DesktopLoadingView(message: "در حال بارگذاری نوازندگان مورد علاقه...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .transition(pageTransition)
                    }
                case .playlists:
                    PlaylistsContentView(
                        playlists: manualPlaylists,
                        onCreatePlaylist: {
                            startCreatePlaylistOnly()
                        },
                        onOpenPlaylist: { playlist in
                            openPlaylistTracksCollection(playlist)
                        },
                        onRenamePlaylist: { playlistId, name in
                            Task {
                                let _ = await DesktopPlaylistDataLoader.renamePlaylist(
                                    playlistId: playlistId,
                                    name: name
                                )
                                await refreshManualPlaylists()
                            }
                        },
                        onDeletePlaylist: { playlistId in
                            withAnimation(.easeInOut(duration: 0.18)) {
                                manualPlaylists.removeAll { $0.id == playlistId }
                            }
                            Task {
                                let success = await DesktopPlaylistDataLoader.deletePlaylist(
                                    playlistId: playlistId
                                )
                                if !success {
                                    await refreshManualPlaylists()
                                    return
                                }
                                await refreshManualPlaylists()
                            }
                        }
                    )
                    .transition(pageTransition)
                case .programTracks:
                    if let category = selectedProgramCategory {
                        if let programTracksRows = selectedProgramTracks {
                            ProgramTracksContentView(
                                title: category.title,
                                badge: selectedProgramTracksBadge.isEmpty ? "مجموعه \(category.title)" : selectedProgramTracksBadge,
                                countText: category.count,
                                duetBanner: selectedProgramTracksDuet,
                                tracks: programTracksRows,
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
                                manualPlaylists: manualPlaylists,
                                onAddTrackToPlaylist: { playlistId, trackId in
                                    Task {
                                        let _ = await DesktopPlaylistDataLoader.addTrack(playlistId: playlistId, trackId: trackId)
                                        await refreshManualPlaylists()
                                    }
                                },
                                onRemoveTrackFromPlaylist: { playlistId, trackId in
                                    if selectedProgramTracksSidebarItem == .myPlaylists {
                                        withAnimation(pageAnimation) {
                                            selectedProgramTracks?.removeAll { row in
                                                row.trackId == trackId
                                            }
                                            if let category = selectedProgramCategory {
                                                let updatedCount = selectedProgramTracks?.count ?? 0
                                                selectedProgramCategory = ProgramItem(
                                                    sourceCategoryId: category.sourceCategoryId,
                                                    title: category.title,
                                                    count: "\(updatedCount) برنامه",
                                                    symbol: category.symbol
                                                )
                                            }
                                        }
                                    }
                                    Task {
                                        let _ = await DesktopPlaylistDataLoader.removeTrack(playlistId: playlistId, trackId: trackId)
                                        await refreshManualPlaylists()
                                    }
                                },
                                onCreatePlaylistAndAddTrack: { trackId in
                                    createPlaylistAndAddTrack(trackId)
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
                            manualPlaylists: manualPlaylists,
                            onAddTrackToPlaylist: { playlistId, trackId in
                                Task {
                                    let _ = await DesktopPlaylistDataLoader.addTrack(playlistId: playlistId, trackId: trackId)
                                    await refreshManualPlaylists()
                                }
                            },
                            onRemoveTrackFromPlaylist: { playlistId, trackId in
                                Task {
                                    let _ = await DesktopPlaylistDataLoader.removeTrack(playlistId: playlistId, trackId: trackId)
                                    await refreshManualPlaylists()
                                }
                            },
                            onCreatePlaylistAndAddTrack: { trackId in
                                createPlaylistAndAddTrack(trackId)
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
                            },
                            manualPlaylists: manualPlaylists,
                            onAddTrackToPlaylist: { playlistId, trackId in
                                Task {
                                    let _ = await DesktopPlaylistDataLoader.addTrack(playlistId: playlistId, trackId: trackId)
                                    await refreshManualPlaylists()
                                }
                            },
                            onRemoveTrackFromPlaylist: { playlistId, trackId in
                                Task {
                                    let _ = await DesktopPlaylistDataLoader.removeTrack(playlistId: playlistId, trackId: trackId)
                                    await refreshManualPlaylists()
                                }
                            },
                            onCreatePlaylistAndAddTrack: { trackId in
                                createPlaylistAndAddTrack(trackId)
                            },
                            favoriteArtistIds: favoriteArtistIds,
                            onToggleArtistFavorite: toggleArtistFavorite
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
        case .favoriteSingers:
            return .artists
        case .search:
            return .search
        case .searchResults:
            return .search
        case .players:
            return .instrumentalists
        case .favoritePlayers:
            return .instrumentalists
        case .programTracks:
            return .programs
        case .playlists:
            return .programs
        case .artistDetails:
            switch artistDetailsSourcePage {
            case .singers:
                return .artists
            case .favoriteSingers:
                return .artists
            case .players:
                return .instrumentalists
            case .favoritePlayers:
                return .instrumentalists
            case .search:
                return .search
            default:
                return .programs
            }
        case .programDetails:
            switch programDetailsSourcePage {
            case .singers:
                return .artists
            case .favoriteSingers:
                return .artists
            case .players:
                return .instrumentalists
            case .favoritePlayers:
                return .instrumentalists
            case .search, .searchResults:
                return .search
            default:
                return .programs
            }
        }
    }

    private var selectedSidebarItem: SidebarMenuItem? {
        switch currentPage {
        case .favoriteSingers:
            return .favoriteSingers
        case .favoritePlayers:
            return .favoritePlayers
        case .playlists:
            return .myPlaylists
        case .programTracks:
            return selectedProgramTracksSidebarItem
        default:
            return nil
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
            selectedProgramTracksBadge: selectedProgramTracksBadge,
            selectedProgramTracksDuet: selectedProgramTracksDuet,
            selectedProgramTracksSidebarItem: selectedProgramTracksSidebarItem,
            selectedProgramTracks: selectedProgramTracks,
            selectedSearchTracks: selectedSearchTracks,
            selectedSearchTotal: selectedSearchTotal,
            activeSearchFilters: activeSearchFilters,
            activeSearchChips: activeSearchChips,
            isProgramTracksLoading: isProgramTracksLoading,
            isSearchResultsLoading: isSearchResultsLoading,
            isArtistDetailsLoading: isArtistDetailsLoading,
            isProgramDetailsLoading: isProgramDetailsLoading
        )
    }

    private func restore(from snapshot: NavigationSnapshot) {
        withAnimation(pageAnimation) {
            selectedArtistDetails = snapshot.selectedArtistDetails
            artistDetailsSourcePage = snapshot.artistDetailsSourcePage
            selectedProgramDetails = snapshot.selectedProgramDetails
            programDetailsSourcePage = snapshot.programDetailsSourcePage
            selectedProgramCategory = snapshot.selectedProgramCategory
            selectedProgramTracksBadge = snapshot.selectedProgramTracksBadge
            selectedProgramTracksDuet = snapshot.selectedProgramTracksDuet
            selectedProgramTracksSidebarItem = snapshot.selectedProgramTracksSidebarItem
            selectedProgramTracks = snapshot.selectedProgramTracks
            selectedSearchTracks = snapshot.selectedSearchTracks
            selectedSearchTotal = snapshot.selectedSearchTotal
            activeSearchFilters = snapshot.activeSearchFilters
            activeSearchChips = snapshot.activeSearchChips
            isProgramTracksLoading = snapshot.isProgramTracksLoading
            isSearchResultsLoading = snapshot.isSearchResultsLoading
            isArtistDetailsLoading = snapshot.isArtistDetailsLoading
            isProgramDetailsLoading = snapshot.isProgramDetailsLoading
            currentPage = snapshot.page
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

    @MainActor
    private func refreshManualPlaylists() async {
        manualPlaylists = await DesktopPlaylistDataLoader.loadManualPlaylists()
    }

    @MainActor
    private func refreshFavoriteArtistIds() async {
        favoriteArtistIds = await DesktopFavoriteArtistsDataLoader.loadFavoriteArtistIds()
    }

    private func toggleArtistFavorite(_ artistId: Int64, _ artistType: String) {
        Task {
            let isFavorite = await MainActor.run { favoriteArtistIds.contains(artistId) }
            let success: Bool
            if isFavorite {
                success = await DesktopFavoriteArtistsDataLoader.removeFavoriteArtist(artistId: artistId)
            } else {
                success = await DesktopFavoriteArtistsDataLoader.addFavoriteArtist(
                    artistId: artistId,
                    artistType: artistType
                )
            }
            guard success else { return }
            await refreshFavoriteArtistIds()
        }
    }

    private func createPlaylistAndAddTrack(_ trackId: Int64) {
        isCreatingPlaylistOnly = false
        pendingPlaylistTrackIds = nil
        canDismissPlaylistDialogByBackdrop = false
        withAnimation(.easeInOut(duration: 0.2)) {
            pendingPlaylistTrackId = trackId
            pendingPlaylistName = ""
        }
        DispatchQueue.main.async {
            isPlaylistNameFieldFocused = true
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
            canDismissPlaylistDialogByBackdrop = true
        }
    }

    private func dismissPlaylistDialog() {
        canDismissPlaylistDialogByBackdrop = false
        withAnimation(.easeInOut(duration: 0.18)) {
            pendingPlaylistTrackId = nil
            pendingPlaylistTrackIds = nil
            isCreatingPlaylistOnly = false
            pendingPlaylistName = ""
        }
        isPlaylistNameFieldFocused = false
    }

    private func confirmCreatePlaylistAndAdd() {
        let targetTrackId = pendingPlaylistTrackId
        let targetTrackIds = pendingPlaylistTrackIds ?? []
        let name = pendingPlaylistName.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !name.isEmpty else { return }

        Task {
            guard let newPlaylistId = await DesktopPlaylistDataLoader.createManualPlaylist(name: name) else { return }
            if let trackId = targetTrackId {
                let _ = await DesktopPlaylistDataLoader.addTrack(playlistId: newPlaylistId, trackId: trackId)
            }
            if !targetTrackIds.isEmpty {
                for trackId in targetTrackIds {
                    let _ = await DesktopPlaylistDataLoader.addTrack(playlistId: newPlaylistId, trackId: trackId)
                }
            }
            await refreshManualPlaylists()
            await MainActor.run {
                dismissPlaylistDialog()
            }
        }
    }

    private func createPlaylistAndAddTracks(_ trackIds: [Int64]) {
        let uniqueTrackIds = Array(Set(trackIds))
        guard !uniqueTrackIds.isEmpty else { return }
        pendingPlaylistTrackId = nil
        pendingPlaylistTrackIds = uniqueTrackIds
        isCreatingPlaylistOnly = false
        canDismissPlaylistDialogByBackdrop = false
        withAnimation(.easeInOut(duration: 0.2)) {
            pendingPlaylistName = ""
        }
        DispatchQueue.main.async {
            isPlaylistNameFieldFocused = true
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
            canDismissPlaylistDialogByBackdrop = true
        }
    }

    private func startCreatePlaylistOnly() {
        pendingPlaylistTrackId = nil
        pendingPlaylistTrackIds = nil
        isCreatingPlaylistOnly = true
        canDismissPlaylistDialogByBackdrop = false
        withAnimation(.easeInOut(duration: 0.2)) {
            pendingPlaylistName = ""
        }
        DispatchQueue.main.async {
            isPlaylistNameFieldFocused = true
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
            canDismissPlaylistDialogByBackdrop = true
        }
    }

    private var playlistCreateDialog: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("ساخت پلی‌لیست جدید")
                .font(.vazir(15, .bold))
                .foregroundStyle(Palette.primary)
                .frame(maxWidth: .infinity, alignment: .leading)

            Text("اسم پلی‌لیست را وارد کنید.")
                .font(.vazir(10))
                .foregroundStyle(Palette.textMuted)
                .frame(maxWidth: .infinity, alignment: .leading)

            DesktopRTLTextField(
                text: $pendingPlaylistName,
                placeholder: "مثلاً: گلهای دلخواه من",
                placeholderColor: NSColor(calibratedWhite: 0.72, alpha: 1.0),
                textColor: NSColor.black,
                isFirstResponder: isPlaylistNameFieldFocused,
                onSubmit: {
                    confirmCreatePlaylistAndAdd()
                }
            )
                .padding(.horizontal, 12)
                .frame(height: 36)
                .background(Color.white, in: RoundedRectangle(cornerRadius: 10, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 10, style: .continuous)
                        .stroke(Palette.border, lineWidth: 1)
                )

            HStack(spacing: 10) {
                Button {
                    dismissPlaylistDialog()
                } label: {
                    Text("انصراف")
                        .font(.vazir(10.5, .bold))
                        .foregroundStyle(Palette.primary)
                        .frame(width: 92, height: 34)
                        .background(Color.white, in: RoundedRectangle(cornerRadius: 9, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 9, style: .continuous)
                                .stroke(Palette.border, lineWidth: 1)
                        )
                }
                .buttonStyle(.plain)

                Button {
                    confirmCreatePlaylistAndAdd()
                } label: {
                    Text(isCreatingPlaylistOnly ? "ساخت لیست" : "ساخت و افزودن")
                        .font(.vazir(10.5, .bold))
                        .foregroundStyle(.white)
                        .frame(width: 124, height: 34)
                        .background(
                            pendingPlaylistName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                                ? Palette.primary.opacity(0.35)
                                : Palette.primary,
                            in: RoundedRectangle(cornerRadius: 9, style: .continuous)
                        )
                }
                .buttonStyle(.plain)
                .disabled(pendingPlaylistName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
            }
            .frame(maxWidth: .infinity, alignment: .trailing)
        }
        .padding(18)
        .frame(width: 380)
        .background(
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .fill(Color.white)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(Palette.border, lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.16), radius: 28, x: 0, y: 14)
        .environment(\.layoutDirection, .rightToLeft)
        .onAppear {
            DispatchQueue.main.async {
                isPlaylistNameFieldFocused = true
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
        selectedProgramTracksBadge = "مجموعه \(category.title)"
        selectedProgramTracksDuet = nil
        selectedProgramTracksSidebarItem = nil
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

        let requestId = UUID().uuidString
        programTracksLoadRequestId = requestId

        Task {
            let start = Date()
            let loaded = await ProgramTracksDataLoader.load(categoryId: categoryId) ?? []
            let elapsed = Date().timeIntervalSince(start)
            let minVisible = 0.3
            if elapsed < minVisible {
                let remainingNs = UInt64((minVisible - elapsed) * 1_000_000_000)
                try? await Task.sleep(nanoseconds: remainingNs)
            }

            guard programTracksLoadRequestId == requestId else { return }
            guard selectedProgramCategory?.id == category.id, currentPage == .programTracks else { return }
            withAnimation(pageAnimation) {
                selectedProgramTracks = loaded
                isProgramTracksLoading = false
            }
        }
    }

    private func openMostPlayedTracksCollection() {
        let category = ProgramItem(
            title: "محبوب‌ترین برنامه‌ها",
            count: "۲۰ برنامه",
            symbol: "star"
        )
        openProgramTracksCollection(
            category: category,
            badge: "بیشترین شنیده‌شده‌ها",
            sidebarItem: .topPrograms
        ) {
            await TrackCollectionsDataLoader.loadMostPlayed(limit: 20)
        }
    }

    private func openRecentlyPlayedTracksCollection() {
        let category = ProgramItem(
            title: "شنیده‌شده‌ها",
            count: "۲۰ برنامه",
            symbol: "clock"
        )
        openProgramTracksCollection(
            category: category,
            badge: "پخش‌شده‌های اخیر",
            sidebarItem: .recentlyPlayed
        ) {
            await TrackCollectionsDataLoader.loadRecentlyPlayed(limit: 20)
        }
    }

    private func openPlaylistTracksCollection(_ playlist: DesktopManualPlaylist) {
        let ids = Array(playlist.trackIds)
        let category = ProgramItem(
            title: playlist.name,
            count: "\(playlist.trackIds.count) برنامه",
            symbol: "music.note.list"
        )
        openProgramTracksCollection(
            category: category,
            badge: "لیست پخش",
            duetBanner: nil,
            sidebarItem: .myPlaylists
        ) {
            await TrackCollectionsDataLoader.loadByTrackIds(ids)
        }
    }

    private func openDuetTracksCollection(_ duet: DuetBannerItem) {
        let title = "\(duet.singer1) و \(duet.singer2)"
        let category = ProgramItem(
            title: title,
            count: "\(duet.trackCount) برنامه",
            symbol: "music.note.list"
        )
        openProgramTracksCollection(
            category: category,
            badge: "دوئت ماندگار",
            duetBanner: duet,
            sidebarItem: nil
        ) {
            await TrackCollectionsDataLoader.loadDuetPrograms(
                singer1: duet.singer1,
                singer2: duet.singer2
            )
        }
    }

    private func openProgramTracksCollection(
        category: ProgramItem,
        badge: String,
        duetBanner: DuetBannerItem? = nil,
        sidebarItem: SidebarMenuItem?,
        loader: @escaping () async -> [TrackRowItem]
    ) {
        backStack.append(makeSnapshot())
        selectedProgramCategory = category
        selectedProgramTracksBadge = badge
        selectedProgramTracksDuet = duetBanner
        selectedProgramTracksSidebarItem = sidebarItem
        selectedProgramTracks = nil
        isProgramTracksLoading = true
        withAnimation(pageAnimation) {
            currentPage = .programTracks
        }

        let requestId = UUID().uuidString
        programTracksLoadRequestId = requestId

        Task {
            let start = Date()
            let loaded = await loader()
            let elapsed = Date().timeIntervalSince(start)
            let minVisible = 0.3
            if elapsed < minVisible {
                let remainingNs = UInt64((minVisible - elapsed) * 1_000_000_000)
                try? await Task.sleep(nanoseconds: remainingNs)
            }

            guard programTracksLoadRequestId == requestId else { return }
            guard currentPage == .programTracks else { return }

            withAnimation(pageAnimation) {
                selectedProgramTracks = Array(loaded.prefix(20))
                isProgramTracksLoading = false
            }
        }
    }

    private func openSearchResults(
        _ filters: SearchProgramFilters,
        chips: [SearchActiveChip],
        pushSnapshot: Bool = true
    ) {
        guard filters.hasAnyFilter else { return }
        activeSearchFilters = filters
        activeSearchChips = chips
        if pushSnapshot {
            backStack.append(makeSnapshot())
        }
        selectedSearchTracks = nil
        selectedSearchTotal = 0
        isSearchResultsLoading = true
        withAnimation(pageAnimation) {
            currentPage = .searchResults
        }

        Task {
            let start = Date()
            let loaded = await SearchDataLoader.searchPrograms(filters: filters)
            let elapsed = Date().timeIntervalSince(start)
            let minVisible = 0.3
            if elapsed < minVisible {
                let remainingNs = UInt64((minVisible - elapsed) * 1_000_000_000)
                try? await Task.sleep(nanoseconds: remainingNs)
            }
            guard currentPage == .searchResults else { return }
            withAnimation(pageAnimation) {
                selectedSearchTracks = loaded?.tracks ?? []
                selectedSearchTotal = loaded?.total ?? 0
                isSearchResultsLoading = false
            }
        }
    }

    private func removeSearchChip(_ chip: SearchActiveChip) {
        var filters = activeSearchFilters
        switch chip.kind {
        case .category:
            if let id = chip.valueId { filters.categoryIds.removeAll { $0 == id } }
        case .singer:
            if let id = chip.valueId { filters.singerIds.removeAll { $0 == id } }
        case .mode:
            if let id = chip.valueId { filters.modeIds.removeAll { $0 == id } }
        case .orchestra:
            if let id = chip.valueId { filters.orchestraIds.removeAll { $0 == id } }
        case .instrument:
            if let id = chip.valueId { filters.instrumentIds.removeAll { $0 == id } }
        case .performer:
            if let id = chip.valueId { filters.performerIds.removeAll { $0 == id } }
        case .poet:
            if let id = chip.valueId { filters.poetIds.removeAll { $0 == id } }
        case .announcer:
            if let id = chip.valueId { filters.announcerIds.removeAll { $0 == id } }
        case .composer:
            if let id = chip.valueId { filters.composerIds.removeAll { $0 == id } }
        case .arranger:
            if let id = chip.valueId { filters.arrangerIds.removeAll { $0 == id } }
        case .orchestraLeader:
            if let id = chip.valueId { filters.orchestraLeaderIds.removeAll { $0 == id } }
        case .transcript:
            filters.transcriptQuery = nil
        }

        activeSearchFilters = filters
        activeSearchChips.removeAll { $0.id == chip.id }

        guard filters.hasAnyFilter else {
            withAnimation(pageAnimation) {
                currentPage = .search
                selectedSearchTracks = nil
                selectedSearchTotal = 0
                isSearchResultsLoading = false
            }
            return
        }

        openSearchResults(filters, chips: activeSearchChips, pushSnapshot: false)
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
