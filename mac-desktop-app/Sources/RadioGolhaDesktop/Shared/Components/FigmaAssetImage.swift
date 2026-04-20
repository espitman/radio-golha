import SwiftUI

struct FigmaAssetImage: View {
    let url: String
    var fallbackSymbol: String? = nil
    var fallbackTint: Color = .white

    var body: some View {
        CachedRemoteImage(url: URL(string: url)) { image in
            image.resizable().scaledToFill()
        } placeholder: {
            fallback
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
