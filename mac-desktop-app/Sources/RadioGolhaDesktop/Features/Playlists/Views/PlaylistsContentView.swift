import SwiftUI

struct PlaylistsContentView: View {
    let playlists: [DesktopManualPlaylist]
    var onCreatePlaylist: () -> Void = {}
    var onOpenPlaylist: (DesktopManualPlaylist) -> Void = { _ in }

    private let columns = Array(repeating: GridItem(.flexible(), spacing: 18), count: 3)

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                heroSection
                    .padding(.horizontal, 48)
                    .padding(.top, 32)

                playlistsSection
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
                    DesktopPlaylistsHeroPatternOverlay()
                        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
                }

            VStack(spacing: 10) {
                Text("مجموعه گلهای رنگارنگ")
                    .font(.vazir(9, .bold))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .background(Palette.secondary.opacity(0.84), in: Capsule())

                Text("لیست‌های من")
                    .font(.vazir(40.5, .bold))
                    .foregroundStyle(.white)

                HStack(spacing: 6) {
                    Image(systemName: "square.stack.3d.up.fill")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundStyle(Palette.secondary)
                    Text("\(toPersianDigits(playlists.count)) لیست منتخب")
                        .font(.vazir(12, .medium))
                        .foregroundStyle(.white.opacity(0.82))
                }
            }
            .padding(.bottom, 42)
        }
        .frame(height: 232)
        .frame(maxWidth: .infinity)
    }

    private var playlistsSection: some View {
        VStack(alignment: .leading, spacing: 18) {
            HStack {
                Text("لیست‌های پخش")
                    .font(.vazir(21, .bold))
                    .foregroundStyle(Palette.primary)

                Spacer(minLength: 0)

                Button {
                    onCreatePlaylist()
                } label: {
                    HStack(spacing: 6) {
                        Image(systemName: "plus.circle.fill")
                            .font(.system(size: 14, weight: .semibold))
                        Text("ایجاد لیست جدید")
                            .font(.vazir(10.5, .bold))
                    }
                    .foregroundStyle(Palette.secondary)
                }
                .buttonStyle(.plain)
            }
            .padding(.bottom, 10)
            .overlay(alignment: .bottom) {
                Rectangle().fill(Palette.border).frame(height: 1)
            }

            if playlists.isEmpty {
                VStack(spacing: 10) {
                    Image(systemName: "music.note.list")
                        .font(.system(size: 24, weight: .medium))
                        .foregroundStyle(Palette.textMuted.opacity(0.75))
                    Text("هنوز پلی‌لیستی ساخته نشده")
                        .font(.vazir(11, .medium))
                        .foregroundStyle(Palette.textMuted)
                }
                .frame(maxWidth: .infinity, minHeight: 180)
                .background(Palette.surfaceLow, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                        .stroke(Palette.border, lineWidth: 1)
                )
            } else {
                LazyVGrid(columns: columns, spacing: 16) {
                    ForEach(playlists) { playlist in
                        playlistCard(playlist)
                    }
                }
            }
        }
    }

    private func playlistCard(_ playlist: DesktopManualPlaylist) -> some View {
        Button {
            onOpenPlaylist(playlist)
        } label: {
            HStack(spacing: 10) {
                ZStack {
                    Circle()
                        .fill(Palette.secondary.opacity(0.1))
                    Image(systemName: "play.fill")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(Palette.secondary)
                }
                .frame(width: 40, height: 40)

                Spacer(minLength: 0)

                VStack(alignment: .leading, spacing: 2) {
                    Text(playlist.name)
                        .font(.vazir(15, .bold))
                        .foregroundStyle(Palette.primary)
                        .lineLimit(1)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    Text("\(toPersianDigits(playlist.trackIds.count)) قطعه موسیقی")
                        .font(.vazir(9))
                        .foregroundStyle(Palette.textMuted)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }

                Image(systemName: "ellipsis")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundStyle(Palette.primary.opacity(0.26))
            }
            .padding(.horizontal, 16)
            .frame(height: 84)
            .background(Palette.surfaceLow, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .stroke(Palette.border.opacity(0.6), lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }

    private func toPersianDigits(_ value: Int) -> String {
        String(value)
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

private struct DesktopPlaylistsHeroPatternOverlay: View {
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
