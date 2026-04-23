import SwiftUI

struct DesktopHeroBanner: View {
    let badge: String
    let title: String
    let subtitle: String
    var height: CGFloat = 232

    var body: some View {
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
                    DesktopHeroPatternOverlay()
                        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
                }

            VStack(alignment: .center, spacing: 10) {
                Text(badge)
                    .font(.vazir(9, .bold))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .background(Palette.secondary.opacity(0.84), in: Capsule())

                Text(title)
                    .font(.vazir(36, .bold))
                    .foregroundStyle(.white)
                    .multilineTextAlignment(.center)

                Text(subtitle)
                    .font(.vazir(11.5, .medium))
                    .foregroundStyle(.white.opacity(0.86))
                    .multilineTextAlignment(.center)
                    .lineLimit(2)
            }
            .padding(.horizontal, 36)
            .padding(.bottom, 34)
            .frame(maxWidth: .infinity, alignment: .center)
        }
        .frame(height: height)
        .frame(maxWidth: .infinity)
    }
}

private struct DesktopHeroPatternOverlay: View {
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
