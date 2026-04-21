import SwiftUI

struct SingersContentView: View {
    let singers: [SingerListItem]
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
                            ForEach(singersPageAlphabet, id: \.self) { letter in
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
        .onChange(of: singersPageAlphabet) { _ in
            if !singersPageAlphabet.contains(selectedLetter) {
                selectedLetter = "همه"
            }
        }
    }

    private var filteredItems: [SingerListItem] {
        guard selectedLetter != "همه" else { return singers }
        return singers.filter { matchesLetter(name: $0.name, letterGroup: selectedLetter) }
    }

    private var singersPageAlphabet: [String] {
        let allGroups = ["الف", "ب", "پ", "ت", "ج", "چ", "ح", "خ", "د", "ر", "ز", "س", "ش", "ع", "ق", "م", "ن", "و", "ه", "ی"]
        let available = allGroups.filter { group in
            singers.contains { matchesLetter(name: $0.name, letterGroup: group) }
        }
        return ["همه"] + available
    }

    private func matchesLetter(name: String, letterGroup: String) -> Bool {
        let trimmed = name.trimmingCharacters(in: .whitespacesAndNewlines)
        guard let first = trimmed.first else { return false }
        let head = String(first)

        switch letterGroup {
        case "الف": return ["ا", "آ", "أ", "إ"].contains(head)
        case "ب": return head == "ب"
        case "پ": return head == "پ"
        case "ت": return head == "ت"
        case "ج": return head == "ج"
        case "چ": return head == "چ"
        case "ح": return head == "ح"
        case "خ": return head == "خ"
        case "د": return head == "د"
        case "ر": return head == "ر"
        case "ز": return head == "ز"
        case "س": return head == "س"
        case "ش": return head == "ش"
        case "ع": return head == "ع"
        case "ق": return head == "ق"
        case "م": return head == "م"
        case "ن": return head == "ن"
        case "و": return head == "و"
        case "ه": return head == "ه"
        case "ی": return ["ی", "ي"].contains(head)
        default: return true
        }
    }
}
