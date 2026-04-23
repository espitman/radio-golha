import SwiftUI

struct SearchResultsContentView: View {
    let tracks: [TrackRowItem]
    let total: Int
    let activeChips: [SearchActiveChip]
    var onRemoveChip: (SearchActiveChip) -> Void = { _ in }
    var onPlayTrack: (TrackRowItem) -> Void = { _ in }
    var onOpenProgram: (TrackRowItem) -> Void = { _ in }
    var onSaveAsPlaylist: () -> Void = {}
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
                    .padding(.top, 24)
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
                    SearchHeroPatternOverlay()
                        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
                }

            VStack(spacing: 8) {
                Text("نتایج جستجو")
                    .font(.vazir(40.5, .bold))
                    .foregroundStyle(.white)

                Text("\(toPersianDigits("\(total)")) برنامه")
                    .font(.vazir(12, .medium))
                    .foregroundStyle(.white.opacity(0.8))
            }
            .padding(.bottom, activeChips.isEmpty ? 44 : 74)

            if !activeChips.isEmpty {
                chipsSection
                    .padding(.horizontal, 22)
                    .padding(.bottom, 14)
            }
        }
        .frame(height: 232)
        .frame(maxWidth: .infinity)
    }

    private var chipsSection: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(activeChips) { chip in
                    HStack(spacing: 6) {
                        Text(chip.label)
                            .font(.vazir(10, .medium))
                            .foregroundStyle(.white)
                            .lineLimit(1)

                        Button {
                            onRemoveChip(chip)
                        } label: {
                            Image(systemName: "xmark")
                                .font(.system(size: 10, weight: .bold))
                                .foregroundStyle(.white)
                        }
                        .buttonStyle(.plain)
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(Palette.secondary, in: Capsule())
                    .overlay(Capsule().stroke(Palette.secondary.opacity(0.95), lineWidth: 1))
                }
            }
            .frame(maxWidth: .infinity, alignment: .trailing)
        }
        .environment(\.layoutDirection, .rightToLeft)
    }

    private var tracksSection: some View {
        VStack(alignment: .leading, spacing: 18) {
            HStack {
                Text("فهرست قطعات")
                    .font(.vazir(18, .bold))
                    .foregroundStyle(Palette.primary)

                Spacer(minLength: 0)

                Button {
                    onSaveAsPlaylist()
                } label: {
                    HStack(spacing: 6) {
                        Image(systemName: "square.and.arrow.down")
                            .font(.system(size: 12, weight: .semibold))
                        Text("ذخیره نتایج به‌عنوان پلی‌لیست")
                            .font(.vazir(10, .bold))
                    }
                    .foregroundStyle(Palette.secondary)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(
                        RoundedRectangle(cornerRadius: 8, style: .continuous)
                            .fill(Palette.secondary.opacity(0.1))
                    )
                }
                .buttonStyle(.plain)
            }
            .padding(.bottom, 8)
            .overlay(alignment: .bottom) {
                Rectangle().fill(Palette.border).frame(height: 1)
            }

            VStack(spacing: 0) {
                ForEach(Array(tracks.enumerated()), id: \.element.id) { index, row in
                    SearchResultTrackRow(
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

    private func toPersianDigits(_ value: String) -> String {
        value
            .replacingOccurrences(of: "0", with: "۰")
            .replacingOccurrences(of: "1", with: "۱")
            .replacingOccurrences(of: "2", with: "۲")
            .replacingOccurrences(of: "3", with: "۳")
            .replacingOccurrences(of: "4", with: "۴")
            .replacingOccurrences(of: "5", with: "۵")
            .replacingOccurrences(of: "6", with: "۶")
            .replacingOccurrences(of: "7", with: "۷")
            .replacingOccurrences(of: "8", with: "۸")
            .replacingOccurrences(of: "9", with: "۹")
    }
}

private struct SearchHeroPatternOverlay: View {
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

private struct SearchResultTrackRow: View {
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
