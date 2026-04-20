import SwiftUI

struct SingersSection: View {
    var body: some View {
        ArtistsGridSection(title: "خوانندگان برجسته", items: HomeMockData.singers, dark: false)
    }
}

struct InstrumentalistsSection: View {
    var body: some View {
        ArtistsGridSection(title: "نوازندگان برجسته", items: HomeMockData.instrumentalists, dark: true)
    }
}

private struct ArtistsGridSection: View {
    let title: String
    let items: [ArtistItem]
    let dark: Bool

    var body: some View {
        VStack(spacing: 32) {
            SectionHeader(title: title, dark: dark)

            HStack(spacing: 32) {
                ForEach(items) { item in
                    ArtistCard(item: item, dark: dark)
                }
            }
        }
    }
}
