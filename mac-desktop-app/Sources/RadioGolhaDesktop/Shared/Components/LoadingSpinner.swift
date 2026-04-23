import SwiftUI

struct LoadingSpinner: View {
    var color: Color
    var size: CGFloat = 14
    var lineWidth: CGFloat = 2.2

    @State private var rotating = false

    var body: some View {
        Circle()
            .trim(from: 0.14, to: 0.92)
            .stroke(
                color,
                style: StrokeStyle(lineWidth: lineWidth, lineCap: .round)
            )
            .frame(width: size, height: size)
            .rotationEffect(.degrees(rotating ? 360 : 0))
            .animation(.linear(duration: 0.75).repeatForever(autoreverses: false), value: rotating)
            .onAppear { rotating = true }
    }
}
