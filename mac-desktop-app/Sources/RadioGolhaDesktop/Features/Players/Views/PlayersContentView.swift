import SwiftUI

struct PlayersContentView: View {
    @State private var selectedInstrument = "همه"
    private let columns = Array(repeating: GridItem(.fixed(208), spacing: 32), count: 4)

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                VStack(spacing: 0) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("نوازندگان")
                            .font(.vazir(27, .bold))
                            .foregroundStyle(Palette.primary)
                        Text("فهرست مشاهیر موسیقی اصیل ایرانی و نوازندگان برجسته برنامه‌های گلها به تفکیک تخصص و ساز.")
                            .font(.vazir(10.5))
                            .foregroundStyle(Color(hex: 0x43474E))
                            .frame(maxWidth: 620, alignment: .leading)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 48)
                    .padding(.top, 32)

                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(HomeMockData.playersPageInstruments, id: \.self) { instrument in
                                Button {
                                    selectedInstrument = instrument
                                } label: {
                                    Text(instrument)
                                        .font(.vazir(10.5, .bold))
                                        .foregroundStyle(selectedInstrument == instrument ? Color.white : Color(hex: 0x43474E))
                                        .padding(.horizontal, 16)
                                        .padding(.vertical, 8)
                                        .background(
                                            selectedInstrument == instrument ? Palette.secondary : Color(hex: 0xEBE8DF),
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
                            PlayerCard(item: item)
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

    private var filteredItems: [PlayerListItem] {
        guard selectedInstrument != "همه" else { return HomeMockData.playersPageItems }
        return HomeMockData.playersPageItems.filter { $0.instrument.contains(selectedInstrument) }
    }
}

private struct PlayerCard: View {
    let item: PlayerListItem
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
                .frame(width: 208, height: 208)
                .clipped()

                LinearGradient(
                    colors: [Palette.primary.opacity(0.62), .clear],
                    startPoint: .bottom,
                    endPoint: .top
                )
                .opacity(isHovered ? 1.0 : 0.0)
                .frame(width: 208, height: 208)

                Text("مشاهده آثار")
                    .font(.vazir(9, .bold))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .overlay(
                        Capsule().stroke(Color.white.opacity(0.4), lineWidth: 1)
                    )
                    .opacity(isHovered ? 1.0 : 0.0)
                    .padding(.bottom, 20)
            }

            VStack(alignment: .leading, spacing: 6) {
                Text(item.name)
                    .font(.vazir(15, .bold))
                    .foregroundStyle(Palette.primary)

                Text(item.instrument)
                    .font(.vazir(10.5, .bold))
                    .foregroundStyle(Palette.secondary)

                HStack(spacing: 6) {
                    Image(systemName: "music.note.list")
                        .font(.system(size: 12, weight: .regular))
                    Text(item.programsCount)
                        .font(.vazir(9))
                }
                .foregroundStyle(Color(hex: 0x43474E))
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(20)
            .background(.white)
        }
        .frame(width: 208, height: 300, alignment: .top)
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .stroke(Palette.border.opacity(0.5), lineWidth: 1)
        )
        .shadow(
            color: isHovered ? Palette.primary.opacity(0.14) : .clear,
            radius: isHovered ? 20 : 0,
            x: 0,
            y: isHovered ? 8 : 0
        )
        .offset(y: isHovered ? -1 : 0)
        .animation(.easeOut(duration: 0.25), value: isHovered)
        .onHover { hovering in
            isHovered = hovering
        }
    }
}
