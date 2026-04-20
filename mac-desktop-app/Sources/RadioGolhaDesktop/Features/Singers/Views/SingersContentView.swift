import SwiftUI

struct SingersContentView: View {
    var onSingerTap: (SingerListItem) -> Void = { _ in }
    @State private var selectedLetter = "همه"
    private let columns = Array(repeating: GridItem(.fixed(208), spacing: 32), count: 4)

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                VStack(spacing: 0) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("خوانندگان")
                            .font(.vazir(27, .bold))
                            .foregroundStyle(Palette.primary)
                        Text("فهرست جامع اساتید، خوانندگان و نوازندگان تاریخ رادیو گلها به ترتیب حروف الفبا.")
                            .font(.vazir(10.5))
                            .foregroundStyle(Color(hex: 0x43474E))
                            .frame(maxWidth: 620, alignment: .leading)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 48)
                    .padding(.top, 32)

                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(HomeMockData.singersPageAlphabet, id: \.self) { letter in
                                Button {
                                    selectedLetter = letter
                                } label: {
                                    Text(letter)
                                        .font(.vazir(10.5, .bold))
                                        .foregroundStyle(selectedLetter == letter ? Color.white : Palette.primary)
                                        .padding(.horizontal, 16)
                                        .padding(.vertical, 8)
                                        .background(
                                            selectedLetter == letter ? Palette.secondary : Palette.sidebar,
                                            in: Capsule()
                                        )
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(.horizontal, 48)
                        .padding(.vertical, 12)
                    }
                    .padding(.top, 28)

                    LazyVGrid(columns: columns, spacing: 32) {
                        ForEach(filteredItems) { item in
                            ArtistCard(
                                item: ArtistItem(
                                    name: item.name,
                                    role: item.programsCount,
                                    imageURL: item.imageURL
                                ),
                                dark: false
                            ) {
                                onSingerTap(item)
                            }
                        }
                    }
                    .padding(.horizontal, 48)
                    .padding(.top, 36)
                    .padding(.bottom, 140)
                }
                .background(
                    ZStack {
                        Palette.surface
                        ShamsehPatternOverlay()
                    }
                )
            }
        }
        .frame(width: 1024)
        .background(Palette.surface)
        .environment(\.layoutDirection, .rightToLeft)
    }

    private var filteredItems: [SingerListItem] {
        guard selectedLetter != "همه" else { return HomeMockData.singersPageItems }
        return HomeMockData.singersPageItems.filter { $0.name.hasPrefix(selectedLetter) }
    }
}
