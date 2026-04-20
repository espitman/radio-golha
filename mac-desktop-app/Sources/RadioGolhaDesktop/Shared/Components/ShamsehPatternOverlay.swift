import SwiftUI

struct ShamsehPatternOverlay: View {
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
