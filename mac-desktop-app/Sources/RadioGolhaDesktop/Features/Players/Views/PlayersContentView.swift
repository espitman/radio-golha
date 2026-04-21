import SwiftUI

struct PlayersContentView: View {
    let players: [PlayerListItem]
    var onPlayerTap: (PlayerListItem) -> Void = { _ in }
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
                            ForEach(playersPageInstruments, id: \.self) { instrument in
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
                            ArtistCard(
                                item: ArtistItem(
                                    sourceArtistId: item.sourceArtistId,
                                    name: item.name,
                                    role: selectedInstrument == "همه"
                                        ? "\(item.instrument) • \(item.programsCount)"
                                        : item.programsCount,
                                    imageURL: item.imageURL
                                ),
                                dark: false
                            ) {
                                onPlayerTap(item)
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
        .onAppear {
            if !playersPageInstruments.contains(selectedInstrument) {
                selectedInstrument = "همه"
            }
        }
        .onChange(of: playersPageInstruments) { _ in
            if !playersPageInstruments.contains(selectedInstrument) {
                selectedInstrument = "همه"
            }
        }
    }

    private var filteredItems: [PlayerListItem] {
        guard selectedInstrument != "همه" else { return players }
        return players.filter { $0.instrument.contains(selectedInstrument) }
    }

    private var playersPageInstruments: [String] {
        let instruments = players
            .map { $0.instrument.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }
        let unique = Array(Set(instruments)).sorted()
        return ["همه"] + unique
    }
}
