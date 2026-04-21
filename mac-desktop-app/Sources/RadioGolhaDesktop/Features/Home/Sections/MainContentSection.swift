import SwiftUI

struct MainContentSection: View {
    private let heroImage = URL(string: "https://lh3.googleusercontent.com/aida-public/AB6AXuBTRoCtbLy1Vpa3t_ez8WfRkhOFnnCGOnbhRCJ3Tw_GbsQa8OqeyyLL2ov1DPWrduyIkRYbX-OfQkwuqlVraQ8QJOLKS5xz0nnGbm6Xcew6EaIxSXymeWEKEzkuhnl0xcQXO5V7KIbFs1M5iwZVA0GNgsIljnkjQYe9AdbIOmQEm8ohOVd39E_qi-b-b39xQ0PVaqyEtPk83DXRnERRtsx7Xs_6iidmtYtqJkpETR82f97iPOnF3stP0rFiR0INpoCfMwIZfPGvTTV3")
    let content: HomeContentData
    var onRefreshTopTracks: () async -> Void = {}
    var onPlayTrack: (TrackRowItem) -> Void = { _ in }
    var currentPlayingTrackId: String? = nil
    var isPlayerPlaying: Bool = false
    var isPlayerLoading: Bool = false
    var onArtistTap: (ArtistItem) -> Void = { _ in }
    var onProgramCategoryTap: (ProgramItem) -> Void = { _ in }
    var onProgramTap: (String) -> Void = { _ in }
    var onShowAllSingers: () -> Void = {}
    var onShowAllInstrumentalists: () -> Void = {}

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                ZStack {
                    Palette.surface
                    MainPatternOverlay()
                }
                .frame(height: 0)

                VStack(spacing: 0) {
                    MainHero(imageURL: heroImage)
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
                        onArtistTap: onArtistTap
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
                        onArtistTap: onArtistTap
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
                        onProgramTap: onProgramTap
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

private struct MainHero: View {
    let imageURL: URL?

    var body: some View {
        ZStack {
            CachedRemoteImage(url: imageURL) { image in
                image.resizable().scaledToFill()
            } placeholder: {
                LinearGradient(
                    colors: [Color(hex: 0x001A2F), Color(hex: 0x003A67)],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            }

            LinearGradient(
                colors: [Palette.primary.opacity(0.92), Palette.primary.opacity(0.45), .clear],
                startPoint: .leading,
                endPoint: .leading
            )

            VStack(alignment: .leading, spacing: 16) {
                Text("برنامه ویژه")
                    .font(.vazir(9, .bold))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .background(Palette.secondary.opacity(0.8), in: RoundedRectangle(cornerRadius: 4, style: .continuous))

                Text("گلهای رنگارنگ")
                    .font(.vazir(27, .bold))
                    .foregroundStyle(.white)

                Text("بشنوید آثاری جاودانه از استاد محمدرضا شجریان و غلامحسین بنان در مجموعه‌ای بی‌نظیر که روح هر شنونده‌ای را جلا می‌دهد.")
                    .font(.vazir(13.5))
                    .foregroundStyle(.white.opacity(0.82))
                    .lineSpacing(4)
                    .multilineTextAlignment(.leading)

                HStack(spacing: 16) {
                    Button {} label: {
                        HStack(spacing: 8) {
                            Image(systemName: "play.fill")
                                .font(.system(size: 12, weight: .bold))
                            Text("پخش آخرین قسمت")
                                .font(.vazir(12, .bold))
                        }
                        .foregroundStyle(.white)
                        .padding(.horizontal, 32)
                        .padding(.vertical, 12)
                        .background(Palette.secondary, in: Capsule())
                    }
                    .buttonStyle(.plain)

                    Button {} label: {
                        Text("مشاهده لیست پخش")
                            .font(.vazir(12, .bold))
                            .foregroundStyle(.white)
                            .padding(.horizontal, 33)
                            .padding(.vertical, 13)
                            .background(Color.white.opacity(0.10), in: Capsule())
                            .overlay(Capsule().stroke(Color.white.opacity(0.2), lineWidth: 1))
                    }
                    .buttonStyle(.plain)
                }
            }
            .frame(width: 564, alignment: .leading)
            .padding(.leading, 64)
        }
        .frame(height: 320)
        .frame(maxWidth: .infinity)
        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
        .multilineTextAlignment(.leading)
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
                        ArtistCard(item: item, dark: false) {
                            onArtistTap(item)
                        }
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
    var onProgramTap: (String) -> Void = { _ in }

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
                onProgramTap: onProgramTap
            )

            ListBlock(
                title: "شنیده شده‌های اخیر",
                rows: latestRows,
                showRefresh: false,
                onPlayTrack: onPlayTrack,
                currentPlayingTrackId: currentPlayingTrackId,
                isPlayerPlaying: isPlayerPlaying,
                isPlayerLoading: isPlayerLoading,
                onProgramTap: onProgramTap
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
    var onProgramTap: (String) -> Void = { _ in }
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
                            onProgramTap: onProgramTap
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
    var onProgramTap: (String) -> Void = { _ in }

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
                    onProgramTap(row.title)
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
