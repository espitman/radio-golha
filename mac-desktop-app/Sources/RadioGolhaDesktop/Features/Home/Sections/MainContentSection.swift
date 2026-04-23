import SwiftUI

struct MainContentSection: View {
    let content: HomeContentData
    var onRefreshTopTracks: () async -> Void = {}
    var onPlayTrack: (TrackRowItem) -> Void = { _ in }
    var currentPlayingTrackId: String? = nil
    var isPlayerPlaying: Bool = false
    var isPlayerLoading: Bool = false
    var onArtistTap: (ArtistItem) -> Void = { _ in }
    var onProgramCategoryTap: (ProgramItem) -> Void = { _ in }
    var onProgramTap: (TrackRowItem) -> Void = { _ in }
    var onDuetTap: (DuetBannerItem) -> Void = { _ in }
    var manualPlaylists: [DesktopManualPlaylist] = []
    var onAddTrackToPlaylist: (Int64, Int64) -> Void = { _, _ in }
    var onRemoveTrackFromPlaylist: (Int64, Int64) -> Void = { _, _ in }
    var onCreatePlaylistAndAddTrack: (Int64) -> Void = { _ in }
    var onShowAllSingers: () -> Void = {}
    var onShowAllInstrumentalists: () -> Void = {}
    var favoriteArtistIds: Set<Int64> = []
    var onToggleArtistFavorite: (Int64, String) -> Void = { _, _ in }

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                ZStack {
                    Palette.surface
                    MainPatternOverlay()
                }
                .frame(height: 0)

                VStack(spacing: 0) {
                    MainDuetsHeroSlider(
                        items: content.duets,
                        onTap: onDuetTap
                    )
                        .padding(.horizontal, 48)
                        .padding(.top, 32)

                    ProgramsGridSection(
                        items: content.programs,
                        onProgramTap: onProgramCategoryTap
                    )
                        .padding(.horizontal, 48)
                        .padding(.top, 48)

                    ArtistsGridSection(
                        title: "خوانندگان برجسته",
                        items: content.singers,
                        showAllAction: true,
                        onShowAllAction: onShowAllSingers,
                        onArtistTap: onArtistTap,
                        favoriteArtistIds: favoriteArtistIds,
                        onToggleArtistFavorite: onToggleArtistFavorite
                    )
                    .padding(.horizontal, 48)
                    .padding(.top, 48)

                    ModesPillsSection(items: content.modes)
                        .padding(.horizontal, 48)
                        .padding(.top, 32)

                    ArtistsGridSection(
                        title: "نوازندگان برجسته",
                        items: content.instrumentalists,
                        showAllAction: true,
                        onShowAllAction: onShowAllInstrumentalists,
                        onArtistTap: onArtistTap,
                        favoriteArtistIds: favoriteArtistIds,
                        onToggleArtistFavorite: onToggleArtistFavorite
                    )
                    .padding(.horizontal, 48)
                    .padding(.top, 48)

                    TopListsSection(
                        topRows: content.topProgramsRows,
                        latestRows: content.latestTracksRows,
                        onRefreshTopTracks: onRefreshTopTracks,
                        onPlayTrack: onPlayTrack,
                        currentPlayingTrackId: currentPlayingTrackId,
                        isPlayerPlaying: isPlayerPlaying,
                        isPlayerLoading: isPlayerLoading,
                        onProgramTap: onProgramTap,
                        manualPlaylists: manualPlaylists,
                        onAddTrackToPlaylist: onAddTrackToPlaylist,
                        onRemoveTrackFromPlaylist: onRemoveTrackFromPlaylist,
                        onCreatePlaylistAndAddTrack: onCreatePlaylistAndAddTrack
                    )
                        .padding(.horizontal, 48)
                        .padding(.top, 48)
                        .padding(.bottom, 140)
                }
                .background(
                    ZStack {
                        Palette.surface
                        MainPatternOverlay()
                    }
                )
            }
        }
        .frame(width: 1024)
        .background(Palette.surface)
        .environment(\.layoutDirection, .rightToLeft)
        .multilineTextAlignment(.leading)
    }
}

private struct MainDuetsHeroSlider: View {
    let items: [DuetBannerItem]
    var onTap: (DuetBannerItem) -> Void = { _ in }
    @State private var currentIndex = 0
    private let timer = Timer.publish(every: 4, on: .main, in: .common).autoconnect()

    private var safeItems: [DuetBannerItem] {
        items.isEmpty ? HomeMockData.duets : items
    }

    var body: some View {
        let slides = safeItems
        let safeIndex = max(0, min(currentIndex, max(0, slides.count - 1)))
        ZStack(alignment: .bottom) {
            if !slides.isEmpty {
                MainDuetCard(item: slides[safeIndex], onTap: onTap)
                    .id(slides[safeIndex].id)
                    .transition(.asymmetric(insertion: .move(edge: .trailing).combined(with: .opacity), removal: .move(edge: .leading).combined(with: .opacity)))
            }

            HStack(spacing: 8) {
                ForEach(Array(slides.enumerated()), id: \.offset) { index, _ in
                    Circle()
                        .fill(index == safeIndex ? Palette.secondary : Color.white.opacity(0.35))
                        .frame(width: index == safeIndex ? 8 : 6, height: index == safeIndex ? 8 : 6)
                }
            }
            .frame(maxWidth: .infinity)
            .padding(.bottom, 10)
        }
        .contentShape(Rectangle())
        .gesture(
            DragGesture(minimumDistance: 20)
                .onEnded { value in
                    guard slides.count > 1 else { return }
                    guard abs(value.translation.width) > abs(value.translation.height) else { return }

                    if value.translation.width < -40 {
                        withAnimation(.easeInOut(duration: 0.35)) {
                            currentIndex = (safeIndex + 1) % slides.count
                        }
                    } else if value.translation.width > 40 {
                        withAnimation(.easeInOut(duration: 0.35)) {
                            currentIndex = (safeIndex - 1 + slides.count) % slides.count
                        }
                    }
                }
        )
        .onReceive(timer) { _ in
            guard slides.count > 1 else { return }
            withAnimation(.easeInOut(duration: 0.35)) {
                currentIndex = (currentIndex + 1) % slides.count
            }
        }
        .onChange(of: items.count) { newValue in
            if newValue == 0 {
                currentIndex = 0
            } else if currentIndex >= newValue {
                currentIndex = 0
            }
        }
    }
}

private struct MainDuetCard: View {
    let item: DuetBannerItem
    var onTap: (DuetBannerItem) -> Void = { _ in }
    @State private var pulse = false
    @State private var ringRotation: Double = 0

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color(hex: 0x041334), Color(hex: 0x08255B), Color(hex: 0x031234)],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )

            Circle()
                .fill(Palette.secondary.opacity(pulse ? 0.10 : 0.06))
                .frame(width: 360, height: 360)
                .offset(x: 260, y: -10)
                .blur(radius: 18)

            Circle()
                .fill(Palette.secondary.opacity(pulse ? 0.08 : 0.05))
                .frame(width: 260, height: 260)
                .offset(x: -220, y: 80)
                .blur(radius: 16)

            HStack(spacing: 20) {
                VStack(alignment: .leading, spacing: 6) {
                    Text("دوئت ماندگار")
                        .font(.vazir(11, .medium))
                        .foregroundStyle(Color.white.opacity(0.45))

                    Text("\(item.singer1) و \(item.singer2)")
                        .font(.vazir(30, .bold))
                        .foregroundStyle(Palette.secondary)
                        .lineLimit(1)
                        .minimumScaleFactor(0.7)

                    if item.trackCount > 0 {
                        Text("\(item.trackCount) ترک")
                            .font(.vazir(12, .medium))
                            .foregroundStyle(Color.white.opacity(0.5))
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                ZStack {
                    RotatingAvatarRing(rotation: ringRotation)
                        .frame(width: 106, height: 106)
                        .offset(x: -34, y: 0)
                    DuetAvatar(name: item.singer1, imageURL: item.singer1Avatar)
                        .frame(width: 100, height: 100)
                        .offset(x: -34, y: 0)

                    RotatingAvatarRing(rotation: -ringRotation)
                        .frame(width: 106, height: 106)
                        .offset(x: 34, y: 0)
                    DuetAvatar(name: item.singer2, imageURL: item.singer2Avatar)
                        .frame(width: 100, height: 100)
                        .offset(x: 34, y: 0)
                }
                .frame(width: 220, height: 120)
            }
            .padding(.horizontal, 30)
            .padding(.vertical, 24)
        }
        .frame(height: 190)
        .frame(maxWidth: .infinity)
        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
        .contentShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
        .onTapGesture {
            onTap(item)
        }
        .onAppear {
            withAnimation(.easeInOut(duration: 2).repeatForever(autoreverses: true)) {
                pulse.toggle()
            }
            withAnimation(.linear(duration: 8).repeatForever(autoreverses: false)) {
                ringRotation = 360
            }
        }
    }
}

private struct RotatingAvatarRing: View {
    let rotation: Double

    var body: some View {
        ZStack {
            Circle()
                .stroke(Palette.secondary.opacity(0.3), lineWidth: 2.5)
            Circle()
                .trim(from: 0, to: 0.24)
                .stroke(Palette.secondary.opacity(0.95), style: StrokeStyle(lineWidth: 2.5, lineCap: .round))
                .rotationEffect(.degrees(rotation))
        }
    }
}

private struct DuetAvatar: View {
    let name: String
    let imageURL: String?

    var body: some View {
        ZStack {
            Circle().fill(Color.white.opacity(0.12))
            if let imageURL, let url = URL(string: imageURL), !imageURL.isEmpty {
                CachedRemoteImage(url: url) { image in
                    image
                        .resizable()
                        .scaledToFill()
                } placeholder: {
                    Circle().fill(Color.white.opacity(0.18))
                }
            } else {
                Image(systemName: "person.fill")
                    .font(.system(size: 30, weight: .regular))
                    .foregroundStyle(Palette.secondary.opacity(0.75))
            }
        }
        .clipShape(Circle())
    }
}

private struct ProgramsGridSection: View {
    let items: [ProgramItem]
    var onProgramTap: (ProgramItem) -> Void = { _ in }

    var body: some View {
        VStack(alignment: .leading, spacing: 32) {
            SectionHead(title: "برنامه‌ها", showAllAction: false)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 24) {
                    ForEach(items) { item in
                        ProgramRowCard(item: item) {
                            onProgramTap(item)
                        }
                    }
                }
                .padding(.vertical, 4)
            }
        }
    }
}

private struct ProgramRowCard: View {
    let item: ProgramItem
    var onTap: () -> Void = {}

    var body: some View {
        HStack(spacing: 16) {
            ZStack {
                RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .fill(Palette.primary.opacity(0.05))
                Image(systemName: item.symbol)
                    .font(.system(size: 24, weight: .regular))
                    .foregroundStyle(Palette.secondary)
            }
            .frame(width: 64, height: 64)

            VStack(alignment: .leading, spacing: 2) {
                Text(item.title.replacingOccurrences(of: "\n", with: " "))
                    .font(.vazir(15.75, .bold))
                    .foregroundStyle(Palette.primary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.85)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text(item.count)
                    .font(.vazir(9))
                    .foregroundStyle(Palette.textMuted)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            Spacer(minLength: 0)
        }
        .padding(16)
        .frame(width: 214, height: 106)
        .background(Palette.surfaceLow, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(Palette.border, lineWidth: 1)
        )
        .contentShape(Rectangle())
        .onTapGesture {
            onTap()
        }
    }
}

private struct ArtistsGridSection: View {
    let title: String
    let items: [ArtistItem]
    let showAllAction: Bool
    let onShowAllAction: () -> Void
    let onArtistTap: (ArtistItem) -> Void
    var favoriteArtistIds: Set<Int64> = []
    var onToggleArtistFavorite: (Int64, String) -> Void = { _, _ in }

    var body: some View {
        VStack(alignment: .leading, spacing: 32) {
            SectionHead(
                title: title,
                showAllAction: showAllAction,
                onShowAllAction: onShowAllAction
            )
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 24) {
                    ForEach(items) { item in
                        ArtistCard(
                            item: item,
                            dark: false,
                            onTap: {
                                onArtistTap(item)
                            },
                            favoriteArtistIds: favoriteArtistIds,
                            onToggleFavorite: onToggleArtistFavorite
                        )
                        .frame(width: 208)
                    }
                }
                .padding(.vertical, 4)
            }
        }
    }
}

private struct ModesPillsSection: View {
    let items: [ModeItem]

    var body: some View {
        VStack(alignment: .leading, spacing: 24) {
            SectionHead(title: "دستگاه‌ها و آوازها", showAllAction: false)

            ScrollViewReader { proxy in
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 16) {
                        ForEach(items) { mode in
                            Text(mode.title)
                                .font(.vazir(10.5))
                                .foregroundStyle(Palette.text)
                                .padding(.horizontal, 32)
                                .padding(.vertical, 12)
                                .background(Palette.surfaceLow, in: Capsule())
                                .overlay(Capsule().stroke(Palette.border, lineWidth: 1))
                                .id(mode.id)
                        }
                    }
                    .padding(.vertical, 4)
                }
                .onAppear {
                    if let first = items.first?.id {
                        proxy.scrollTo(first, anchor: .leading)
                    }
                }
            }
        }
    }
}

private struct TopListsSection: View {
    let topRows: [TrackRowItem]
    let latestRows: [TrackRowItem]
    var onRefreshTopTracks: () async -> Void = {}
    var onPlayTrack: (TrackRowItem) -> Void = { _ in }
    var currentPlayingTrackId: String? = nil
    var isPlayerPlaying: Bool = false
    var isPlayerLoading: Bool = false
    var onProgramTap: (TrackRowItem) -> Void = { _ in }
    var manualPlaylists: [DesktopManualPlaylist] = []
    var onAddTrackToPlaylist: (Int64, Int64) -> Void = { _, _ in }
    var onRemoveTrackFromPlaylist: (Int64, Int64) -> Void = { _, _ in }
    var onCreatePlaylistAndAddTrack: (Int64) -> Void = { _ in }

    var body: some View {
        HStack(alignment: .top, spacing: 48) {
            ListBlock(
                title: "برترین برنامه‌ها",
                rows: topRows,
                showRefresh: true,
                onRefresh: onRefreshTopTracks,
                onPlayTrack: onPlayTrack,
                currentPlayingTrackId: currentPlayingTrackId,
                isPlayerPlaying: isPlayerPlaying,
                isPlayerLoading: isPlayerLoading,
                onProgramTap: onProgramTap,
                manualPlaylists: manualPlaylists,
                onAddTrackToPlaylist: onAddTrackToPlaylist,
                onRemoveTrackFromPlaylist: onRemoveTrackFromPlaylist,
                onCreatePlaylistAndAddTrack: onCreatePlaylistAndAddTrack
            )

            ListBlock(
                title: "شنیده شده‌های اخیر",
                rows: latestRows,
                showRefresh: false,
                onPlayTrack: onPlayTrack,
                currentPlayingTrackId: currentPlayingTrackId,
                isPlayerPlaying: isPlayerPlaying,
                isPlayerLoading: isPlayerLoading,
                onProgramTap: onProgramTap,
                manualPlaylists: manualPlaylists,
                onAddTrackToPlaylist: onAddTrackToPlaylist,
                onRemoveTrackFromPlaylist: onRemoveTrackFromPlaylist,
                onCreatePlaylistAndAddTrack: onCreatePlaylistAndAddTrack
            )
        }
    }
}

private struct ListBlock: View {
    let title: String
    let rows: [TrackRowItem]
    let showRefresh: Bool
    var onRefresh: () async -> Void = {}
    var onPlayTrack: (TrackRowItem) -> Void = { _ in }
    var currentPlayingTrackId: String? = nil
    var isPlayerPlaying: Bool = false
    var isPlayerLoading: Bool = false
    var onProgramTap: (TrackRowItem) -> Void = { _ in }
    var manualPlaylists: [DesktopManualPlaylist] = []
    var onAddTrackToPlaylist: (Int64, Int64) -> Void = { _, _ in }
    var onRemoveTrackFromPlaylist: (Int64, Int64) -> Void = { _, _ in }
    var onCreatePlaylistAndAddTrack: (Int64) -> Void = { _ in }
    @State private var isRefreshing = false

    var body: some View {
        VStack(alignment: .leading, spacing: 24) {
            HStack {
                Text(title)
                    .font(.vazir(22.5, .bold))
                    .foregroundStyle(Palette.primary)

                Spacer(minLength: 0)

                if showRefresh {
                    Button {
                        guard !isRefreshing else { return }
                        Task {
                            isRefreshing = true
                            await onRefresh()
                            isRefreshing = false
                        }
                    } label: {
                        Image(systemName: "arrow.clockwise")
                            .font(.system(size: 18, weight: .regular))
                            .foregroundStyle(Palette.secondary)
                            .rotationEffect(.degrees(isRefreshing ? 360 : 0))
                            .animation(
                                isRefreshing
                                ? .linear(duration: 0.8).repeatForever(autoreverses: false)
                                : .default,
                                value: isRefreshing
                            )
                    }
                    .buttonStyle(.plain)
                    .disabled(isRefreshing)
                }
            }

            VStack(spacing: 0) {
                if rows.isEmpty && title == "شنیده شده‌های اخیر" {
                    EmptyRecentRow()
                } else {
                    ForEach(Array(rows.enumerated()), id: \.offset) { index, row in
                        ListRow(
                            row: row,
                            isActive: currentPlayingTrackId == row.id,
                            isPlayerPlaying: isPlayerPlaying,
                            isPlayerLoading: isPlayerLoading,
                            onPlayTrack: onPlayTrack,
                            onProgramTap: onProgramTap,
                            manualPlaylists: manualPlaylists,
                            onAddTrackToPlaylist: onAddTrackToPlaylist,
                            onRemoveTrackFromPlaylist: onRemoveTrackFromPlaylist,
                            onCreatePlaylistAndAddTrack: onCreatePlaylistAndAddTrack
                        )
                        if index < rows.count - 1 {
                            Divider().overlay(Palette.text.opacity(0.06))
                        }
                    }
                }
            }
            .background(Palette.surfaceLow, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .stroke(Palette.border, lineWidth: 1)
            )
        }
        .frame(width: 440)
        .multilineTextAlignment(.leading)
    }
}

private struct EmptyRecentRow: View {
    var body: some View {
        VStack(spacing: 10) {
            Image(systemName: "music.note.list")
                .font(.system(size: 20, weight: .regular))
                .foregroundStyle(Palette.secondary.opacity(0.75))
            Text("ترکی وجود ندارد")
                .font(.vazir(10.5, .bold))
                .foregroundStyle(Palette.textMuted)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 360)
    }
}

private struct ListRow: View {
    let row: TrackRowItem
    let isActive: Bool
    let isPlayerPlaying: Bool
    let isPlayerLoading: Bool
    var onPlayTrack: (TrackRowItem) -> Void = { _ in }
    var onProgramTap: (TrackRowItem) -> Void = { _ in }
    var manualPlaylists: [DesktopManualPlaylist] = []
    var onAddTrackToPlaylist: (Int64, Int64) -> Void = { _, _ in }
    var onRemoveTrackFromPlaylist: (Int64, Int64) -> Void = { _, _ in }
    var onCreatePlaylistAndAddTrack: (Int64) -> Void = { _ in }

    var body: some View {
        HStack(spacing: 16) {
            Button {
                onPlayTrack(row)
            } label: {
                ZStack {
                    RoundedRectangle(cornerRadius: 20, style: .continuous)
                        .fill(Palette.primary.opacity(0.05))
                    if isActive && isPlayerLoading {
                        LoadingSpinner(color: Palette.primary, size: 13, lineWidth: 2.1)
                    } else {
                        Image(systemName: isActive && isPlayerPlaying ? "pause.fill" : "play.fill")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundStyle(Palette.primary)
                    }
                }
                .frame(width: 40, height: 40)
            }
            .buttonStyle(.plain)

            VStack(alignment: .leading, spacing: 2) {
                Button {
                    onProgramTap(row)
                } label: {
                    Text(row.title)
                        .font(.vazir(10.5, .bold))
                        .foregroundStyle(Palette.primary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                .buttonStyle(.plain)
                Text(row.subtitle)
                    .font(.vazir(9))
                    .foregroundStyle(Palette.textMuted)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }

            Text(row.duration)
                .font(.vazir(9))
                .foregroundStyle(Palette.textMuted)
                .frame(width: 48, alignment: .leading)
        }
        .padding(.horizontal, 16)
        .frame(height: 72)
        .trackPlaylistContextMenu(
            trackId: row.trackId,
            playlists: manualPlaylists,
            onAddToPlaylist: onAddTrackToPlaylist,
            onRemoveFromPlaylist: onRemoveTrackFromPlaylist,
            onCreatePlaylistAndAdd: onCreatePlaylistAndAddTrack
        )
    }
}

private struct SectionHead: View {
    let title: String
    let showAllAction: Bool
    var onShowAllAction: (() -> Void)? = nil

    var body: some View {
        HStack {
            Text(title)
                .font(.vazir(22.5, .bold))
                .foregroundStyle(Palette.primary)
                .multilineTextAlignment(.leading)

            Spacer(minLength: 0)

            if showAllAction {
                Button {
                    onShowAllAction?()
                } label: {
                    HStack(spacing: 4) {
                        Image(systemName: "arrow.left")
                            .font(.system(size: 10, weight: .semibold))
                        Text("نمایش همه")
                            .font(.vazir(10.5, .semibold))
                    }
                    .foregroundStyle(Palette.secondary)
                    .environment(\.layoutDirection, .leftToRight)
                }
                .buttonStyle(.plain)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct MainPatternOverlay: View {
    var body: some View {
        GeometryReader { geo in
            let cols = Int(geo.size.width / 120) + 1
            let rows = Int(geo.size.height / 120) + 1
            ZStack {
                ForEach(0..<rows, id: \.self) { r in
                    ForEach(0..<cols, id: \.self) { c in
                        Image(systemName: "sparkle")
                            .font(.system(size: 28, weight: .thin))
                            .foregroundStyle(Palette.secondary.opacity(0.05))
                            .position(x: CGFloat(c) * 120 + 40, y: CGFloat(r) * 120 + 40)
                    }
                }
            }
        }
        .allowsHitTesting(false)
    }
}
