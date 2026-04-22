import SwiftUI
import AppKit

struct PlaylistsContentView: View {
    let playlists: [DesktopManualPlaylist]
    var onCreatePlaylist: () -> Void = {}
    var onOpenPlaylist: (DesktopManualPlaylist) -> Void = { _ in }
    var onRenamePlaylist: (Int64, String) -> Void = { _, _ in }
    var onDeletePlaylist: (Int64) -> Void = { _ in }

    private let columns = Array(repeating: GridItem(.flexible(), spacing: 18), count: 3)
    @State private var menuPlaylistId: Int64? = nil
    @State private var renameTarget: DesktopManualPlaylist? = nil
    @State private var deleteTarget: DesktopManualPlaylist? = nil
    @State private var renameText: String = ""
    @State private var renameFieldFocused: Bool = false

    var body: some View {
        ZStack {
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
            .contentShape(Rectangle())
            .onTapGesture {
                if menuPlaylistId != nil {
                    menuPlaylistId = nil
                }
            }

            if let target = renameTarget {
                modalBackdrop
                renameDialog(for: target)
                    .transition(.opacity.combined(with: .scale(scale: 0.96)))
                    .zIndex(60_001)
            }

            if let target = deleteTarget {
                modalBackdrop
                deleteConfirmDialog(for: target)
                    .transition(.opacity.combined(with: .scale(scale: 0.96)))
                    .zIndex(60_001)
            }
        }
        .frame(width: 1024)
        .background(Palette.surface)
        .environment(\.layoutDirection, .rightToLeft)
        .animation(.easeInOut(duration: 0.18), value: menuPlaylistId)
        .animation(.easeInOut(duration: 0.2), value: renameTarget != nil)
        .animation(.easeInOut(duration: 0.2), value: deleteTarget != nil)
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

            Button {
                menuPlaylistId = (menuPlaylistId == playlist.id) ? nil : playlist.id
            } label: {
                ZStack {
                    RoundedRectangle(cornerRadius: 8, style: .continuous)
                        .fill(Palette.primary.opacity(0.06))
                        .opacity(menuPlaylistId == playlist.id ? 1 : 0)
                    Image(systemName: "ellipsis")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundStyle(Palette.primary.opacity(0.4))
                }
                .frame(width: 32, height: 32)
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 16)
        .frame(height: 84)
        .background(Palette.surfaceLow, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .stroke(Palette.border.opacity(0.6), lineWidth: 1)
        )
        .contentShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        .onTapGesture {
            onOpenPlaylist(playlist)
        }
        .overlay(alignment: .topTrailing) {
            if menuPlaylistId == playlist.id {
                playlistCardMenu(for: playlist.id)
                    .frame(width: 184)
                    .offset(x: -16, y: 60)
                    .transition(.opacity.combined(with: .scale(scale: 0.95, anchor: .topTrailing)))
                    .zIndex(50_001)
            }
        }
        .zIndex(menuPlaylistId == playlist.id ? 50_001 : 0)
    }

    private func playlistCardMenu(for playlistId: Int64) -> some View {
        VStack(spacing: 4) {
            Button {
                menuPlaylistId = nil
                guard let target = playlists.first(where: { $0.id == playlistId }) else { return }
                renameText = target.name
                renameTarget = target
                DispatchQueue.main.async {
                    renameFieldFocused = true
                }
            } label: {
                HStack(spacing: 8) {
                    Image(systemName: "pencil")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundStyle(Palette.primary)
                        .frame(width: 14)
                    Text("تغییر نام")
                        .font(.vazir(9.5, .bold))
                        .foregroundStyle(Palette.primary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    Spacer(minLength: 0)
                }
                .padding(.horizontal, 10)
                .frame(height: 30)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)

            Button {
                menuPlaylistId = nil
                deleteTarget = playlists.first(where: { $0.id == playlistId })
            } label: {
                HStack(spacing: 8) {
                    Image(systemName: "trash")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundStyle(Color(hex: 0xA73333))
                        .frame(width: 14)
                    Text("حذف")
                        .font(.vazir(9.5, .bold))
                        .foregroundStyle(Color(hex: 0xA73333))
                        .frame(maxWidth: .infinity, alignment: .leading)
                    Spacer(minLength: 0)
                }
                .padding(.horizontal, 10)
                .frame(height: 30)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
        }
        .padding(6)
        .background(Color.white, in: RoundedRectangle(cornerRadius: 10, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 10, style: .continuous)
                .stroke(Palette.border, lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.14), radius: 18, x: 0, y: 8)
    }

    private var modalBackdrop: some View {
        Color.black.opacity(0.22)
            .ignoresSafeArea()
            .zIndex(60_000)
            .transition(.opacity)
    }

    private func renameDialog(for target: DesktopManualPlaylist) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("تغییر نام پلی‌لیست")
                .font(.vazir(15, .bold))
                .foregroundStyle(Palette.primary)

            DesktopRTLTextField(
                text: $renameText,
                placeholder: "نام جدید",
                placeholderColor: NSColor(calibratedWhite: 0.72, alpha: 1.0),
                textColor: NSColor.black,
                isFirstResponder: renameFieldFocused,
                onSubmit: {
                    commitRename(target)
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
                    renameTarget = nil
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
                    commitRename(target)
                } label: {
                    Text("ذخیره")
                        .font(.vazir(10.5, .bold))
                        .foregroundStyle(.white)
                        .frame(width: 106, height: 34)
                        .background(
                            renameText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                                ? Palette.primary.opacity(0.35)
                                : Palette.primary,
                            in: RoundedRectangle(cornerRadius: 9, style: .continuous)
                        )
                }
                .buttonStyle(.plain)
                .disabled(renameText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
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
    }

    private func deleteConfirmDialog(for target: DesktopManualPlaylist) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("حذف پلی‌لیست")
                .font(.vazir(15, .bold))
                .foregroundStyle(Palette.primary)

            Text("آیا از حذف «\(target.name)» مطمئن هستید؟")
                .font(.vazir(10.5))
                .foregroundStyle(Palette.textMuted)

            HStack(spacing: 10) {
                Button {
                    deleteTarget = nil
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
                    deleteTarget = nil
                    onDeletePlaylist(target.id)
                } label: {
                    Text("حذف")
                        .font(.vazir(10.5, .bold))
                        .foregroundStyle(.white)
                        .frame(width: 106, height: 34)
                        .background(Color(hex: 0xA73333), in: RoundedRectangle(cornerRadius: 9, style: .continuous))
                }
                .buttonStyle(.plain)
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
    }

    private func commitRename(_ target: DesktopManualPlaylist) {
        let trimmed = renameText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        renameTarget = nil
        onRenamePlaylist(target.id, trimmed)
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
