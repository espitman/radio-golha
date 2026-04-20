import SwiftUI

struct MainContentSection: View {
    private let heroImage = URL(string: "https://lh3.googleusercontent.com/aida-public/AB6AXuBTRoCtbLy1Vpa3t_ez8WfRkhOFnnCGOnbhRCJ3Tw_GbsQa8OqeyyLL2ov1DPWrduyIkRYbX-OfQkwuqlVraQ8QJOLKS5xz0nnGbm6Xcew6EaIxSXymeWEKEzkuhnl0xcQXO5V7KIbFs1M5iwZVA0GNgsIljnkjQYe9AdbIOmQEm8ohOVd39E_qi-b-b39xQ0PVaqyEtPk83DXRnERRtsx7Xs_6iidmtYtqJkpETR82f97iPOnF3stP0rFiR0INpoCfMwIZfPGvTTV3")
    let content: HomeContentData
    var onArtistTap: (ArtistItem) -> Void = { _ in }
    var onProgramTap: (String) -> Void = { _ in }

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                ZStack {
                    Palette.surface
                    MainPatternOverlay()
                }
                .frame(height: 0)

                VStack(spacing: 0) {
                    MainHero(imageURL: heroImage)
                        .padding(.horizontal, 48)
                        .padding(.top, 32)

                    ProgramsGridSection(items: content.programs)
                        .padding(.horizontal, 48)
                        .padding(.top, 48)

                    ArtistsGridSection(
                        title: "خوانندگان برجسته",
                        items: content.singers,
                        showAllAction: true,
                        onArtistTap: onArtistTap
                    )
                    .padding(.horizontal, 48)
                    .padding(.top, 48)

                    ModesPillsSection(items: content.modes)
                        .padding(.horizontal, 48)
                        .padding(.top, 32)

                    ArtistsGridSection(
                        title: "نوازندگان برجسته",
                        items: content.instrumentalists,
                        showAllAction: true,
                        onArtistTap: onArtistTap
                    )
                    .padding(.horizontal, 48)
                    .padding(.top, 48)

                    TopListsSection(
                        topRows: content.topProgramsRows,
                        latestRows: content.latestTracksRows,
                        onProgramTap: onProgramTap
                    )
                        .padding(.horizontal, 48)
                        .padding(.top, 48)
                        .padding(.bottom, 140)
                }
                .background(
                    ZStack {
                        Palette.surface
                        MainPatternOverlay()
                    }
                )
            }
        }
        .frame(width: 1024)
        .background(Palette.surface)
        .environment(\.layoutDirection, .rightToLeft)
        .multilineTextAlignment(.leading)
    }
}

private struct MainHero: View {
    let imageURL: URL?

    var body: some View {
        ZStack {
            AsyncImage(url: imageURL) { phase in
                switch phase {
                case .success(let image):
                    image.resizable().scaledToFill()
                default:
                    LinearGradient(
                        colors: [Color(hex: 0x001A2F), Color(hex: 0x003A67)],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                }
            }

            LinearGradient(
                colors: [Palette.primary.opacity(0.92), Palette.primary.opacity(0.45), .clear],
                startPoint: .leading,
                endPoint: .leading
            )

            VStack(alignment: .leading, spacing: 16) {
                Text("برنامه ویژه")
                    .font(.vazir(9, .bold))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .background(Palette.secondary.opacity(0.8), in: RoundedRectangle(cornerRadius: 4, style: .continuous))

                Text("گلهای رنگارنگ")
                    .font(.vazir(27, .bold))
                    .foregroundStyle(.white)

                Text("بشنوید آثاری جاودانه از استاد محمدرضا شجریان و غلامحسین بنان در مجموعه‌ای بی‌نظیر که روح هر شنونده‌ای را جلا می‌دهد.")
                    .font(.vazir(13.5))
                    .foregroundStyle(.white.opacity(0.82))
                    .lineSpacing(4)
                    .multilineTextAlignment(.leading)

                HStack(spacing: 16) {
                    Button {} label: {
                        HStack(spacing: 8) {
                            Image(systemName: "play.fill")
                                .font(.system(size: 12, weight: .bold))
                            Text("پخش آخرین قسمت")
                                .font(.vazir(12, .bold))
                        }
                        .foregroundStyle(.white)
                        .padding(.horizontal, 32)
                        .padding(.vertical, 12)
                        .background(Palette.secondary, in: Capsule())
                    }
                    .buttonStyle(.plain)

                    Button {} label: {
                        Text("مشاهده لیست پخش")
                            .font(.vazir(12, .bold))
                            .foregroundStyle(.white)
                            .padding(.horizontal, 33)
                            .padding(.vertical, 13)
                            .background(Color.white.opacity(0.10), in: Capsule())
                            .overlay(Capsule().stroke(Color.white.opacity(0.2), lineWidth: 1))
                    }
                    .buttonStyle(.plain)
                }
            }
            .frame(width: 564, alignment: .leading)
            .padding(.leading, 64)
        }
        .frame(height: 320)
        .frame(maxWidth: .infinity)
        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
        .multilineTextAlignment(.leading)
    }
}

private struct ProgramsGridSection: View {
    let items: [ProgramItem]
    private let columns = Array(repeating: GridItem(.fixed(214), spacing: 24), count: 4)

    var body: some View {
        VStack(alignment: .leading, spacing: 32) {
            SectionHead(title: "برنامه‌ها", showAllAction: true)

            LazyVGrid(columns: columns, spacing: 24) {
                ForEach(items) { item in
                    ProgramRowCard(item: item)
                }
            }
        }
    }
}

private struct ProgramRowCard: View {
    let item: ProgramItem

    var body: some View {
        HStack(spacing: 16) {
            ZStack {
                RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .fill(Palette.primary.opacity(0.05))
                Image(systemName: item.symbol)
                    .font(.system(size: 24, weight: .regular))
                    .foregroundStyle(Palette.secondary)
            }
            .frame(width: 64, height: 64)

            VStack(alignment: .leading, spacing: 2) {
                Text(item.title.replacingOccurrences(of: "\n", with: " "))
                    .font(.vazir(15.75, .bold))
                    .foregroundStyle(Palette.primary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.85)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text(item.count)
                    .font(.vazir(9))
                    .foregroundStyle(Palette.textMuted)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            Spacer(minLength: 0)
        }
        .padding(16)
        .frame(width: 214, height: 106)
        .background(Palette.surfaceLow, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(Palette.border, lineWidth: 1)
        )
    }
}

private struct ArtistsGridSection: View {
    let title: String
    let items: [ArtistItem]
    let showAllAction: Bool
    let onArtistTap: (ArtistItem) -> Void
    private let columns = Array(repeating: GridItem(.fixed(208), spacing: 32), count: 4)

    var body: some View {
        VStack(alignment: .leading, spacing: 32) {
            SectionHead(title: title, showAllAction: showAllAction)
            LazyVGrid(columns: columns, spacing: 32) {
                ForEach(items) { item in
                    ArtistCard(item: item, dark: false) {
                        onArtistTap(item)
                    }
                }
            }
        }
    }
}

private struct ModesPillsSection: View {
    let items: [ModeItem]

    var body: some View {
        VStack(alignment: .leading, spacing: 24) {
            Text("دستگاه‌ها و آوازها")
                .font(.vazir(18, .bold))
                .foregroundStyle(Palette.primary)
                .frame(maxWidth: .infinity, alignment: .leading)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 16) {
                    ForEach(items) { mode in
                        Text(mode.title)
                            .font(.vazir(10.5))
                            .foregroundStyle(Palette.text)
                            .padding(.horizontal, 32)
                            .padding(.vertical, 12)
                            .background(Palette.surfaceLow, in: Capsule())
                            .overlay(Capsule().stroke(Palette.border, lineWidth: 1))
                    }
                }
                .padding(.vertical, 4)
            }
        }
    }
}

private struct TopListsSection: View {
    let topRows: [TrackRowItem]
    let latestRows: [TrackRowItem]
    var onProgramTap: (String) -> Void = { _ in }

    var body: some View {
        HStack(spacing: 48) {
            ListBlock(
                title: "برترین برنامه‌ها",
                rows: topRows,
                showRefresh: true,
                onProgramTap: onProgramTap
            )

            ListBlock(
                title: "شنیده شده‌های اخیر",
                rows: latestRows,
                showRefresh: false,
                onProgramTap: onProgramTap
            )
        }
    }
}

private struct ListBlock: View {
    let title: String
    let rows: [TrackRowItem]
    let showRefresh: Bool
    var onProgramTap: (String) -> Void = { _ in }

    var body: some View {
        VStack(alignment: .leading, spacing: 24) {
            HStack {
                Text(title)
                    .font(.vazir(22.5, .bold))
                    .foregroundStyle(Palette.primary)

                Spacer(minLength: 0)

                if showRefresh {
                    Image(systemName: "arrow.clockwise")
                        .font(.system(size: 18, weight: .regular))
                        .foregroundStyle(Palette.secondary)
                }
            }

            VStack(spacing: 0) {
                ForEach(Array(rows.enumerated()), id: \.offset) { index, row in
                    ListRow(row: row, onProgramTap: onProgramTap)
                    if index < rows.count - 1 {
                        Divider().overlay(Palette.text.opacity(0.06))
                    }
                }
            }
            .background(Palette.surfaceLow, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .stroke(Palette.border, lineWidth: 1)
            )
        }
        .frame(width: 440)
        .multilineTextAlignment(.leading)
    }
}

private struct ListRow: View {
    let row: TrackRowItem
    var onProgramTap: (String) -> Void = { _ in }

    var body: some View {
        HStack(spacing: 16) {
            ZStack {
                RoundedRectangle(cornerRadius: 20, style: .continuous)
                    .fill(Palette.primary.opacity(0.05))
                Image(systemName: "play.fill")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundStyle(Palette.primary)
            }
            .frame(width: 40, height: 40)

            VStack(alignment: .leading, spacing: 2) {
                Button {
                    onProgramTap(row.title)
                } label: {
                    Text(row.title)
                        .font(.vazir(10.5, .bold))
                        .foregroundStyle(Palette.primary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                .buttonStyle(.plain)
                Text(row.subtitle)
                    .font(.vazir(9))
                    .foregroundStyle(Palette.textMuted)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }

            Text(row.duration)
                .font(.vazir(9))
                .foregroundStyle(Palette.textMuted)
                .frame(width: 48, alignment: .leading)
        }
        .padding(.horizontal, 16)
        .frame(height: 72)
    }
}

private struct SectionHead: View {
    let title: String
    let showAllAction: Bool

    var body: some View {
        HStack {
            Text(title)
                .font(.vazir(22.5, .bold))
                .foregroundStyle(Palette.primary)
                .multilineTextAlignment(.leading)

            Spacer(minLength: 0)

            if showAllAction {
                HStack(spacing: 4) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 10, weight: .semibold))
                    Text("مشاهده همه")
                        .font(.vazir(10.5, .semibold))
                }
                .foregroundStyle(Palette.secondary)
                .environment(\.layoutDirection, .leftToRight)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct MainPatternOverlay: View {
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
