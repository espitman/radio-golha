import SwiftUI

struct ProgramTracksContentView: View {
    let title: String
    let badge: String
    let countText: String
    var duetBanner: DuetBannerItem? = nil
    let tracks: [TrackRowItem]
    var onPlayTrack: (TrackRowItem) -> Void = { _ in }
    var onOpenProgram: (TrackRowItem) -> Void = { _ in }
    var manualPlaylists: [DesktopManualPlaylist] = []
    var onAddTrackToPlaylist: (Int64, Int64) -> Void = { _, _ in }
    var onRemoveTrackFromPlaylist: (Int64, Int64) -> Void = { _, _ in }
    var onCreatePlaylistAndAddTrack: (Int64) -> Void = { _ in }
    var currentPlayingTrackId: String? = nil
    var isPlayerPlaying: Bool = false
    var isPlayerLoading: Bool = false

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                heroSection
                    .padding(.horizontal, 48)
                    .padding(.top, 32)

                tracksSection
                    .padding(.horizontal, 48)
                    .padding(.top, 36)
                    .padding(.bottom, 140)
            }
            .background(
                ZStack {
                    Palette.surface
                    ShamsehPatternOverlay()
                }
            )
        }
        .frame(width: 1024)
        .background(Palette.surface)
        .environment(\.layoutDirection, .rightToLeft)
    }

    private var heroSection: some View {
        Group {
            if let duetBanner {
                ProgramTracksDuetHero(duet: duetBanner)
            } else {
                ZStack(alignment: .bottom) {
                    RoundedRectangle(cornerRadius: 24, style: .continuous)
                        .fill(
                            LinearGradient(
                                colors: [Color(hex: 0x001A2F), Color(hex: 0x002E56), Color(hex: 0x001A2F)],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .overlay {
                            Rectangle()
                                .fill(
                                    LinearGradient(
                                        colors: [Color.white.opacity(0.08), Color.clear],
                                        startPoint: .top,
                                        endPoint: .bottom
                                    )
                                )
                        }
                        .overlay {
                            HeroPatternOverlay()
                                .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
                        }

                    VStack(spacing: 8) {
                        Text(badge)
                            .font(.vazir(9, .bold))
                            .foregroundStyle(.white)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 4)
                            .background(Palette.secondary.opacity(0.8), in: Capsule())

                        Text(title)
                            .font(.vazir(40.5, .bold))
                            .foregroundStyle(.white)

                        Text(countText)
                            .font(.vazir(12, .medium))
                            .foregroundStyle(.white.opacity(0.8))
                    }
                    .padding(.bottom, 54)
                }
                .frame(height: 232)
                .frame(maxWidth: .infinity)
            }
        }
    }

    private var tracksSection: some View {
        VStack(alignment: .leading, spacing: 18) {
            HStack {
                Text("فهرست قطعات")
                    .font(.vazir(18, .bold))
                    .foregroundStyle(Palette.primary)
                Spacer(minLength: 0)
            }
            .padding(.bottom, 8)
            .overlay(alignment: .bottom) {
                Rectangle().fill(Palette.border).frame(height: 1)
            }

            VStack(spacing: 0) {
                ForEach(Array(tracks.enumerated()), id: \.element.id) { index, row in
                    ProgramTrackRow(
                        row: row,
                        isActive: currentPlayingTrackId == row.id,
                        isPlayerPlaying: isPlayerPlaying,
                        isPlayerLoading: isPlayerLoading,
                        onPlay: { onPlayTrack(row) },
                        onOpen: { onOpenProgram(row) },
                        manualPlaylists: manualPlaylists,
                        onAddTrackToPlaylist: onAddTrackToPlaylist,
                        onRemoveTrackFromPlaylist: onRemoveTrackFromPlaylist,
                        onCreatePlaylistAndAddTrack: onCreatePlaylistAndAddTrack
                    )
                    if index < tracks.count - 1 {
                        Divider().overlay(Palette.text.opacity(0.06))
                    }
                }
            }
            .background(Palette.surfaceLow, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .stroke(Palette.border, lineWidth: 1)
            )
        }
    }
}

private struct ProgramTracksDuetHero: View {
    let duet: DuetBannerItem
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

                    Text("\(duet.singer1) و \(duet.singer2)")
                        .font(.vazir(30, .bold))
                        .foregroundStyle(Palette.secondary)
                        .lineLimit(1)
                        .minimumScaleFactor(0.7)

                    if duet.trackCount > 0 {
                        Text("\(duet.trackCount) ترک")
                            .font(.vazir(12, .medium))
                            .foregroundStyle(Color.white.opacity(0.5))
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                ZStack {
                    Circle()
                        .stroke(Palette.secondary.opacity(0.3), lineWidth: 2.5)
                        .overlay(
                            Circle()
                                .trim(from: 0, to: 0.24)
                                .stroke(Palette.secondary.opacity(0.95), style: StrokeStyle(lineWidth: 2.5, lineCap: .round))
                                .rotationEffect(.degrees(ringRotation))
                        )
                        .frame(width: 106, height: 106)
                        .offset(x: -34, y: 0)
                    DuetHeroAvatar(name: duet.singer1, imageURL: duet.singer1Avatar)
                        .frame(width: 100, height: 100)
                        .offset(x: -34, y: 0)

                    Circle()
                        .stroke(Palette.secondary.opacity(0.3), lineWidth: 2.5)
                        .overlay(
                            Circle()
                                .trim(from: 0, to: 0.24)
                                .stroke(Palette.secondary.opacity(0.95), style: StrokeStyle(lineWidth: 2.5, lineCap: .round))
                                .rotationEffect(.degrees(-ringRotation))
                        )
                        .frame(width: 106, height: 106)
                        .offset(x: 34, y: 0)
                    DuetHeroAvatar(name: duet.singer2, imageURL: duet.singer2Avatar)
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

private struct DuetHeroAvatar: View {
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

private struct HeroPatternOverlay: View {
    var body: some View {
        GeometryReader { geo in
            let cols = Int(geo.size.width / 84) + 1
            let rows = Int(geo.size.height / 84) + 1
            ZStack {
                ForEach(0..<rows, id: \.self) { r in
                    ForEach(0..<cols, id: \.self) { c in
                        Image(systemName: "sparkle")
                            .font(.system(size: 20, weight: .thin))
                            .foregroundStyle(Color.white.opacity(0.08))
                            .position(x: CGFloat(c) * 84 + 30, y: CGFloat(r) * 84 + 26)
                    }
                }
            }
        }
        .allowsHitTesting(false)
    }
}

private struct ProgramTrackRow: View {
    let row: TrackRowItem
    let isActive: Bool
    let isPlayerPlaying: Bool
    let isPlayerLoading: Bool
    var onPlay: () -> Void = {}
    var onOpen: () -> Void = {}
    var manualPlaylists: [DesktopManualPlaylist] = []
    var onAddTrackToPlaylist: (Int64, Int64) -> Void = { _, _ in }
    var onRemoveTrackFromPlaylist: (Int64, Int64) -> Void = { _, _ in }
    var onCreatePlaylistAndAddTrack: (Int64) -> Void = { _ in }

    var body: some View {
        HStack(spacing: 14) {
            Button {
                onPlay()
            } label: {
                ZStack {
                    Circle()
                        .fill(Palette.primary.opacity(0.05))
                    if isActive && isPlayerLoading {
                        LoadingSpinner(color: Palette.primary, size: 12, lineWidth: 2)
                    } else {
                        Image(systemName: isActive && isPlayerPlaying ? "pause.fill" : "play.fill")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundStyle(Palette.primary)
                    }
                }
                .frame(width: 40, height: 40)
            }
            .buttonStyle(.plain)

            VStack(alignment: .leading, spacing: 2) {
                Button {
                    onOpen()
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
                .frame(width: 56, alignment: .leading)
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
