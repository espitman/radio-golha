import SwiftUI

struct SingersContentView: View {
    var onSelectTab: (DesktopMainTab) -> Void = { _ in }
    @State private var selectedLetter = "همه"
    private let columns = Array(repeating: GridItem(.fixed(208), spacing: 32), count: 4)

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                DesktopTopNavigationBar(
                    selectedTab: .artists,
                    onSelectTab: onSelectTab
                )

                VStack(spacing: 0) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("خوانندگان")
                            .font(.vazir(37.5, .bold))
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
                                            selectedLetter == letter ? Palette.primary : Palette.sidebar,
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
                            SingerListCard(item: item)
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

private struct SingerListCard: View {
    let item: SingerListItem
    @State private var isHovered = false

    var body: some View {
        VStack(spacing: 0) {
            ZStack(alignment: .bottom) {
                AsyncImage(url: URL(string: item.imageURL)) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .scaledToFill()
                            .grayscale(isHovered ? 0.0 : 0.85)
                            .scaleEffect(isHovered ? 1.03 : 1.0)
                    default:
                        Rectangle().fill(Color(hex: 0xE5E2DA))
                    }
                }
                .frame(width: 206, height: 206)
                .clipped()

                LinearGradient(
                    colors: [Palette.primary.opacity(0.72), .clear],
                    startPoint: .bottom,
                    endPoint: .top
                )
                .opacity(isHovered ? 1.0 : 0.0)
                .frame(width: 206, height: 206)

                Text("مشاهده آثار")
                    .font(.vazir(9, .bold))
                    .foregroundStyle(Color(hex: 0x4E3D00))
                    .padding(.horizontal, 24)
                    .padding(.vertical, 8)
                    .background(Color(hex: 0xFED488), in: Capsule())
                    .padding(.bottom, 20)
                    .opacity(isHovered ? 1.0 : 0.0)
                    .offset(y: isHovered ? 0 : 8)
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(item.name)
                    .font(.vazir(15, .bold))
                    .foregroundStyle(Palette.primary)
                Text(item.programsCount)
                    .font(.vazir(10.5))
                    .foregroundStyle(Color(hex: 0x43474E))
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(20)
            .background(.white)
        }
        .frame(width: 208, height: 300)
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .stroke(Palette.border, lineWidth: 1)
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
    }
}
