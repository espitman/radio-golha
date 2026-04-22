import SwiftUI

enum SidebarMenuItem: Hashable {
    case favoriteSingers
    case favoritePlayers
    case myPlaylists
    case topPrograms
    case recentlyPlayed
}

struct SidebarSection: View {
    var selectedItem: SidebarMenuItem? = nil
    var onSelectItem: (SidebarMenuItem) -> Void = { _ in }

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
                navItem("خواننده‌های مورد علاقه", "heart", item: .favoriteSingers)
                navItem("نوازندگان مورد علاقه", "music.mic", item: .favoritePlayers)
                navItem("پلی لیست‌های من", "music.note.list", item: .myPlaylists)
                navItem("محبوب‌ترین برنامه‌ها", "star", item: .topPrograms)
                navItem("شنیده‌شده‌ها", "clock.arrow.circlepath", item: .recentlyPlayed)
            }

            Spacer(minLength: 0)

            SidebarFooter()
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
