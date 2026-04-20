import SwiftUI

struct FigmaAssetImage: View {
    let url: String
    var fallbackSymbol: String? = nil
    var fallbackTint: Color = .white

    var body: some View {
        AsyncImage(url: URL(string: url)) { phase in
            switch phase {
            case .success(let image):
                image.resizable().scaledToFill()
            case .failure, .empty:
                fallback
            @unknown default:
                fallback
            }
        }
        .clipped()
    }

    @ViewBuilder
    private var fallback: some View {
        if let fallbackSymbol {
            Image(systemName: fallbackSymbol)
                .resizable()
                .scaledToFit()
                .foregroundStyle(fallbackTint)
                .padding(1)
        } else {
            Color.clear
        }
    }
}
