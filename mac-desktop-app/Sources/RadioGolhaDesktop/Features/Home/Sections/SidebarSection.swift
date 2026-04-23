import SwiftUI

enum SidebarMenuItem: Hashable {
    case home
    case singers
    case players
    case search
    case favoriteSingers
    case favoritePlayers
    case myPlaylists
    case topPrograms
    case recentlyPlayed
    case settings
    case help
}

struct SidebarSection: View {
    var selectedItem: SidebarMenuItem? = nil
    var onSelectItem: (SidebarMenuItem) -> Void = { _ in }
    var onOpenSettings: () -> Void = {}
    var onOpenHelp: () -> Void = {}

    var body: some View {
        VStack(spacing: 0) {
            VStack(alignment: .leading, spacing: 4) {
                Text("رادیو گلها")
                    .font(.vazir(18, .bold))
                    .foregroundStyle(Palette.primaryMuted)
                Text("میراث موسیقی اصیل ایران")
                    .font(.vazir(9))
                    .foregroundStyle(Palette.text.opacity(0.6))
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 32)
            .padding(.top, 32)
            .padding(.bottom, 48)

            VStack(spacing: 8) {
                navItem("صفحه اصلی", "house", item: .home)
                navItem("خواننده‌ها", "music.microphone", item: .singers)
                navItem("نوازندگان", "music.mic", item: .players)
                navItem("شنیده‌شده‌های اخیر", "clock.arrow.circlepath", item: .recentlyPlayed)
                navItem("جستجوی پیشرفته", "magnifyingglass", item: .search)
            }

            Spacer(minLength: 0)

            SidebarFooter(
                selectedItem: selectedItem,
                onOpenSettings: onOpenSettings,
                onOpenHelp: onOpenHelp
            )
        }
        .background(Palette.sidebar)
        .multilineTextAlignment(.leading)
    }

    private func navItem(_ title: String, _ icon: String, item: SidebarMenuItem) -> some View {
        let active = selectedItem == item

        return Button {
            onSelectItem(item)
        } label: {
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundStyle(active ? Palette.secondary : Palette.text.opacity(0.9))
                Text(title)
                    .font(.vazir(10.5, active ? .bold : .regular))
                    .foregroundStyle(active ? Palette.secondary : Palette.text)
            }
            .padding(.vertical, 12)
            .padding(.horizontal, 32)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(active ? Palette.surfaceLow : .clear)
            .overlay(alignment: .leading) {
                if active {
                    Rectangle().fill(Palette.secondary).frame(width: 4)
                }
            }
        }
        .buttonStyle(.plain)
    }
}
