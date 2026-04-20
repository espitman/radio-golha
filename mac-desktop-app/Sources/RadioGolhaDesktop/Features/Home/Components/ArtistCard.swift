import SwiftUI

struct ArtistCard: View {
    let item: ArtistItem
    var dark: Bool = false

    var body: some View {
        VStack(spacing: 0) {
            ZStack(alignment: .bottom) {
                AsyncImage(url: URL(string: item.imageURL)) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .scaledToFill()
                            .grayscale(0.85)
                    default:
                        Rectangle().fill(dark ? Color(hex: 0x111111) : Color(hex: 0xE5E2DA))
                    }
                }
                .frame(width: 206, height: 206)
                .clipped()

                if !dark {
                    LinearGradient(
                        colors: [Palette.primary.opacity(0.72), .clear],
                        startPoint: .bottom,
                        endPoint: .top
                    )
                    .opacity(0.0)
                    .frame(width: 206, height: 206)

                    Text("مشاهده آثار")
                        .font(.vazir(9, .bold))
                        .foregroundStyle(Color(hex: 0x4E3D00))
                        .padding(.horizontal, 24)
                        .padding(.vertical, 8)
                        .background(Color(hex: 0xFED488), in: Capsule())
                        .padding(.bottom, 20)
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
        .frame(width: 208, height: 300)
        .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 8, style: .continuous)
                .stroke(Color(hex: 0xC4C6CF).opacity(dark ? 0.15 : 0.10), lineWidth: 1)
        )
    }
}
