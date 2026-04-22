import SwiftUI

struct ArtistCard: View {
    let item: ArtistItem
    var dark: Bool = false
    var onTap: (() -> Void)? = nil
    var favoriteArtistIds: Set<Int64> = []
    var onToggleFavorite: ((Int64, String) -> Void)? = nil
    @State private var isHovered = false
    private let cardWidth: CGFloat = 208

    var body: some View {
        VStack(spacing: 0) {
            ZStack(alignment: .bottom) {
                CachedRemoteImage(url: URL(string: item.imageURL)) { image in
                    image
                        .resizable()
                        .scaledToFill()
                        .grayscale(isHovered ? 0.0 : 0.85)
                        .scaleEffect(isHovered ? 1.03 : 1.0)
                } placeholder: {
                    Rectangle().fill(dark ? Color(hex: 0x111111) : Color(hex: 0xE5E2DA))
                }
                .frame(width: cardWidth, height: cardWidth)
                .clipped()

                if !dark {
                    LinearGradient(
                        colors: [Palette.primary.opacity(0.72), .clear],
                        startPoint: .bottom,
                        endPoint: .top
                    )
                    .opacity(isHovered ? 1.0 : 0.0)
                    .frame(width: cardWidth, height: cardWidth)

                    Text("مشاهده آثار")
                        .font(.vazir(9, .bold))
                        .foregroundStyle(Color(hex: 0x4E3D00))
                        .padding(.horizontal, 24)
                        .padding(.vertical, 8)
                        .background(Color(hex: 0xFED488), in: Capsule())
                        .padding(.bottom, 20)
                        .opacity(isHovered ? 1.0 : 0.0)
                        .offset(y: isHovered ? 0 : 8)
                        .allowsHitTesting(isHovered)
                }
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(item.name.replacingOccurrences(of: "‌", with: " "))
                    .font(.vazir(16.5, .bold))
                    .foregroundStyle(dark ? .white : Palette.primary)
                Text(item.role)
                    .font(.vazir(10.5))
                    .foregroundStyle(dark ? .white.opacity(0.7) : Color(hex: 0x43474E))
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .multilineTextAlignment(.leading)
            .padding(20)
            .background(dark ? Color(hex: 0x181818) : .white)
        }
        .frame(width: cardWidth, height: 300, alignment: .top)
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .stroke(Color(hex: 0xC4C6CF).opacity(dark ? 0.15 : 0.10), lineWidth: 1)
        )
        .shadow(
            color: isHovered ? Palette.primary.opacity(0.14) : .clear,
            radius: isHovered ? 20 : 0,
            x: 0,
            y: isHovered ? 8 : 0
        )
        .animation(.easeOut(duration: 0.25), value: isHovered)
        .onHover { hovering in
            isHovered = hovering
        }
        .contentShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        .onTapGesture {
            onTap?()
        }
        .contextMenu {
            if let sourceArtistId = item.sourceArtistId, let onToggleFavorite {
                let isFavorite = favoriteArtistIds.contains(sourceArtistId)
                Button(isFavorite ? "حذف از لیست مورد علاقه" : "افزودن به لیست مورد علاقه") {
                    onToggleFavorite(sourceArtistId, artistTypeForFavorite())
                }
            }
        }
    }

    private func artistTypeForFavorite() -> String {
        if item.role.contains("نوازنده") { return "performer" }
        if item.role.contains("شاعر") { return "poet" }
        if item.role.contains("گوینده") { return "announcer" }
        if item.role.contains("آهنگساز") { return "composer" }
        if item.role.contains("تنظیم") { return "arranger" }
        return "singer"
    }
}
