import SwiftUI

struct ArtistDetailsContentView: View {
    let artist: ArtistDetailsItem
    var onBack: () -> Void = {}
    var onOpenArtist: (ArtistCollaboratorItem) -> Void = { _ in }
    var onOpenProgram: (ArtistProgramRow) -> Void = { _ in }
    var onPlayTrack: (ArtistProgramRow) -> Void = { _ in }
    var manualPlaylists: [DesktopManualPlaylist] = []
    var onAddTrackToPlaylist: (Int64, Int64) -> Void = { _, _ in }
    var onRemoveTrackFromPlaylist: (Int64, Int64) -> Void = { _, _ in }
    var onCreatePlaylistAndAddTrack: (Int64) -> Void = { _ in }
    var currentPlayingTrackId: String? = nil
    var isPlayerPlaying: Bool = false
    var isPlayerLoading: Bool = false
    private let collaboratorsColumns = Array(repeating: GridItem(.fixed(104), spacing: 12), count: 2)

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                topSection
                    .padding(.horizontal, 48)
                    .padding(.top, 32)

                bodySection
                    .padding(.horizontal, 48)
                    .padding(.top, 32)
                    .padding(.bottom, 140)
            }
            .background(
                ZStack {
                    Palette.surface
                    ShamsehPatternOverlay()
                }
            )
        }
        .coordinateSpace(name: "artistDetailsScroll")
        .id(artist.id)
        .frame(width: 1024)
        .background(Palette.surface)
        .environment(\.layoutDirection, .rightToLeft)
    }

    private var topSection: some View {
        HStack(alignment: .center, spacing: 44) {
            ZStack(alignment: .bottom) {
                FigmaAssetImage(url: artist.imageURL)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .grayscale(1.0)

                LinearGradient(
                    colors: [Palette.primary.opacity(0.8), .clear],
                    startPoint: .bottom,
                    endPoint: .top
                )
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
            .frame(width: 280, height: 280)
            .clipped()

            VStack(alignment: .leading, spacing: 14) {
                Text(artist.name)
                    .font(.vazir(45, .bold))
                    .foregroundStyle(Palette.secondary)
                    .tracking(-0.3)
                    .frame(maxWidth: .infinity, alignment: .leading)

                Button {
                } label: {
                    HStack(spacing: 6) {
                        Image(systemName: "heart.fill")
                            .font(.system(size: 12, weight: .semibold))
                        Text("افزودن به علاقه‌مندی‌ها")
                            .font(.vazir(10.5, .bold))
                    }
                    .foregroundStyle(.white)
                    .padding(.horizontal, 18)
                    .padding(.vertical, 8)
                    .background(Palette.secondary, in: Capsule())
                }
                .buttonStyle(.plain)
                .padding(.vertical, 4)

                HStack(spacing: 42) {
                    VStack(spacing: 2) {
                        Text(artist.totalProgramsText)
                            .font(.vazir(18, .bold))
                            .foregroundStyle(Palette.primary)
                        Text("کل برنامه‌ها")
                            .font(.vazir(7.5))
                            .foregroundStyle(Color(hex: 0x6D706F))
                    }
                    .frame(minWidth: 50)

                    ForEach(artist.stats) { stat in
                        VStack(spacing: 2) {
                            Text(stat.value)
                                .font(.vazir(18, .bold))
                                .foregroundStyle(Palette.primary)
                            Text(stat.label)
                                .font(.vazir(7.5))
                                .foregroundStyle(Color(hex: 0x6D706F))
                        }
                        .frame(minWidth: 50)
                    }
                }
            }
            .frame(maxWidth: .infinity, minHeight: 280, maxHeight: 280, alignment: .center)
        }
    }

    private var bodySection: some View {
        HStack(alignment: .top, spacing: 24) {
            programsPanel
                .frame(width: 670)

            StickySidePanel {
                sidePanel
            }
            .frame(width: 234)
        }
    }

    private var programsPanel: some View {
        VStack(alignment: .leading, spacing: 16) {
            panelHeader(title: "برنامه‌ها", fontSize: 18)

            VStack(spacing: 0) {
                ForEach(Array(artist.programs.enumerated()), id: \.element.id) { index, row in
                    ProgramRow(
                        row: row,
                        isActive: currentPlayingTrackId == playbackId(for: row),
                        isPlayerPlaying: isPlayerPlaying,
                        isPlayerLoading: isPlayerLoading,
                        onPlayTrack: {
                            onPlayTrack(row)
                        }
                    ) {
                        onOpenProgram(row)
                    }
                    .trackPlaylistContextMenu(
                        trackId: row.trackId,
                        playlists: manualPlaylists,
                        onAddToPlaylist: onAddTrackToPlaylist,
                        onRemoveFromPlaylist: onRemoveTrackFromPlaylist,
                        onCreatePlaylistAndAdd: onCreatePlaylistAndAddTrack
                    )
                    if index < artist.programs.count - 1 {
                        Divider().overlay(Color(hex: 0x1C1C17).opacity(0.06))
                    }
                }
            }
            .background(Palette.surfaceLow, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .stroke(Palette.border, lineWidth: 1)
            )
        }
    }

    private var sidePanel: some View {
        VStack(alignment: .leading, spacing: 32) {
            VStack(alignment: .leading, spacing: 16) {
                panelHeader(title: "همکاران", fontSize: 15)

                LazyVGrid(columns: collaboratorsColumns, spacing: 12) {
                    ForEach(artist.collaborators) { collaborator in
                        ArtistCard(
                            item: ArtistItem(
                                sourceArtistId: nil,
                                name: collaborator.name,
                                role: collaborator.role,
                                imageURL: collaborator.imageURL
                            ),
                            dark: false
                        ) {
                            onOpenArtist(collaborator)
                        }
                        .scaleEffect(0.5, anchor: .topLeading)
                        .frame(width: 104, height: 150, alignment: .topLeading)
                    }
                }
            }

            VStack(alignment: .leading, spacing: 14) {
                panelHeader(title: "دستگاه‌های شاخص", fontSize: 15)

                HStack(spacing: 6) {
                    ForEach(Array(artist.featuredModes.prefix(4)), id: \.self) { mode in
                        Text(mode)
                            .font(.vazir(7.5, .bold))
                            .lineLimit(1)
                            .foregroundStyle(Palette.primary)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 5)
                            .background(Palette.sidebar, in: Capsule())
                            .overlay(
                                Capsule().stroke(Palette.primary.opacity(0.08), lineWidth: 1)
                            )
                    }
                    Spacer(minLength: 0)
                }
            }
        }
    }

    private func panelHeader(title: String, fontSize: CGFloat) -> some View {
        VStack(spacing: 8) {
            Text(title)
                .font(.vazir(fontSize, .bold))
                .foregroundStyle(Palette.primary)
                .frame(maxWidth: .infinity, alignment: .leading)
                .frame(height: 28, alignment: .bottomLeading)

            Rectangle()
                .fill(Color(hex: 0xE5E2DA))
                .frame(height: 1)
        }
    }

    private func playbackId(for row: ArtistProgramRow) -> String {
        if let trackId = row.trackId {
            return "track-\(trackId)"
        }
        return "artist-\(artist.id)-\(row.title)-\(row.subtitle)-\(row.duration)"
    }
}

private struct StickySidePanel<Content: View>: View {
    @ViewBuilder var content: Content

    var body: some View {
        GeometryReader { proxy in
            let minY = proxy.frame(in: .named("artistDetailsScroll")).minY
            let yOffset = max(-minY, 0)

            VStack {
                content
            }
            .offset(y: yOffset)
        }
    }
}

private struct ProgramRow: View {
    let row: ArtistProgramRow
    let isActive: Bool
    let isPlayerPlaying: Bool
    let isPlayerLoading: Bool
    var onPlayTrack: () -> Void = {}
    var onOpenProgram: () -> Void = {}

    var body: some View {
        HStack(spacing: 12) {
            Button {
                onPlayTrack()
            } label: {
                ZStack {
                    Circle()
                        .fill(Palette.primary.opacity(0.05))
                    if isActive && isPlayerLoading {
                        LoadingSpinner(color: Palette.primary, size: 11, lineWidth: 2)
                    } else {
                        Image(systemName: isActive && isPlayerPlaying ? "pause.fill" : "play.fill")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundStyle(Palette.primary)
                    }
                }
                .frame(width: 30, height: 30)
            }
            .buttonStyle(.plain)

            VStack(alignment: .leading, spacing: 2) {
                Button {
                    onOpenProgram()
                } label: {
                    Text(row.title)
                        .font(.vazir(10.5, .bold))
                        .foregroundStyle(Palette.primary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                .buttonStyle(.plain)
                Text(row.subtitle)
                    .font(.vazir(9))
                    .foregroundStyle(Color(hex: 0x78716C))
                    .frame(maxWidth: .infinity, alignment: .leading)
            }

            Text(row.duration)
                .font(.vazir(9))
                .foregroundStyle(Color(hex: 0xA8A29E))
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 13)
        .contentShape(Rectangle())
    }
}

private struct FlowLayout<Data: RandomAccessCollection, Content: View>: View where Data.Element: Hashable {
    let spacing: CGFloat
    let lineSpacing: CGFloat
    let items: Data
    let content: (Data.Element) -> Content

    init(spacing: CGFloat, lineSpacing: CGFloat, items: Data, @ViewBuilder content: @escaping (Data.Element) -> Content) {
        self.spacing = spacing
        self.lineSpacing = lineSpacing
        self.items = items
        self.content = content
    }

    var body: some View {
        VStack(alignment: .leading, spacing: lineSpacing) {
            let rows = chunkedItems(maxPerRow: 3)
            ForEach(rows.indices, id: \.self) { rowIndex in
                HStack(spacing: spacing) {
                    ForEach(rows[rowIndex], id: \.self) { item in
                        content(item)
                    }
                    Spacer(minLength: 0)
                }
            }
        }
    }

    private func chunkedItems(maxPerRow: Int) -> [[Data.Element]] {
        guard maxPerRow > 0 else { return [Array(items)] }
        var result: [[Data.Element]] = []
        var current: [Data.Element] = []

        for item in items {
            current.append(item)
            if current.count == maxPerRow {
                result.append(current)
                current = []
            }
        }

        if !current.isEmpty {
            result.append(current)
        }

        return result
    }
}
