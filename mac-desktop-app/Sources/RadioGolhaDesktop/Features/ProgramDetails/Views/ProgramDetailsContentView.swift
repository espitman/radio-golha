import SwiftUI

struct ProgramDetailsContentView: View {
    private enum ContentTab {
        case timeline
        case lyrics
    }

    let program: ProgramDetailsItem
    @ObservedObject var player: DesktopAudioPlayer
    var onBack: () -> Void = {}
    @State private var selectedTab: ContentTab = .timeline

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                heroSection
                    .padding(.horizontal, 48)
                    .padding(.top, 32)

                artistGridSection
                    .padding(.horizontal, 48)
                    .padding(.top, 30)

                timelineSection
                    .padding(.horizontal, 48)
                    .padding(.top, 24)
                    .padding(.bottom, 140)
            }
            .background(
                ZStack {
                    Palette.surface
                    ShamsehPatternOverlay()
                }
            )
        }
        .frame(width: 1024)
        .background(Palette.surface)
        .environment(\.layoutDirection, .leftToRight)
    }

    private var heroSection: some View {
        HStack(alignment: .top, spacing: 36) {
            FigmaAssetImage(url: program.coverImageURL)
                .frame(width: 240, height: 240)

            VStack(alignment: .leading, spacing: 0) {
                Text(program.title)
                    .font(.vazir(27, .bold))
                    .foregroundStyle(Palette.primary)
                    .multilineTextAlignment(.trailing)
                    .frame(maxWidth: .infinity, alignment: .trailing)

                Spacer(minLength: 0)

                HStack(spacing: 0) {
                    statItem("دستگاه / آواز", program.modeTitle)
                    statDivider
                    statItem("زمان کل", program.totalDuration)
                    statDivider
                    statItem("ارکستر", program.orchestra)
                }
                .padding(.vertical, 12)
                .frame(maxWidth: .infinity)
                .overlay(
                    VStack(spacing: 0) {
                        Rectangle().fill(Color(hex: 0xE5E2DA)).frame(height: 1)
                        Spacer()
                        Rectangle().fill(Color(hex: 0xE5E2DA)).frame(height: 1)
                    }
                )
                .padding(.top, 22)

                Spacer(minLength: 0)

                compactPlayback
            }
            .frame(maxWidth: .infinity, minHeight: 240, maxHeight: 240, alignment: .topLeading)
        }
        .environment(\.layoutDirection, .leftToRight)
    }

    private func statItem(_ label: String, _ value: String) -> some View {
        VStack(spacing: 3) {
            Text(label)
                .font(.vazir(10.5, .bold))
                .foregroundStyle(Color(hex: 0x6B7280))
            Text(value)
                .font(.vazir(15, .bold))
                .foregroundStyle(Palette.primary)
        }
        .frame(maxWidth: .infinity)
        .multilineTextAlignment(.center)
    }

    private var statDivider: some View {
        Rectangle()
            .fill(Color(hex: 0xE5E2DA))
            .frame(width: 1, height: 42)
    }

    private var compactPlayback: some View {
        HStack(spacing: 14) {
            Button {
                if isProgramActive {
                    player.togglePlayPause()
                } else {
                    player.play(track: playbackTrack)
                }
            } label: {
                ZStack {
                    Circle()
                        .fill(Palette.primaryMuted)
                        .frame(width: 36, height: 36)
                    if isProgramActive && player.isLoading {
                        LoadingSpinner(color: .black, size: 12, lineWidth: 2.2)
                    } else {
                        Image(systemName: isProgramActive && player.isPlaying ? "pause.fill" : "play.fill")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundStyle(.white)
                    }
                }
            }
            .buttonStyle(.plain)
            .disabled(program.audioURL?.isEmpty != false)
            .opacity(program.audioURL?.isEmpty == false ? 1 : 0.4)

            VStack(spacing: 4) {
                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        Capsule().fill(Color(hex: 0xDAD8D0)).frame(height: 6)
                        Capsule()
                            .fill(Palette.primary)
                            .frame(width: max(0, min(geo.size.width, geo.size.width * currentProgress)), height: 6)
                    }
                    .contentShape(Rectangle())
                    .gesture(
                        DragGesture(minimumDistance: 0)
                            .onChanged { value in
                                guard isProgramActive, player.duration > 0 else { return }
                                let x = min(max(value.location.x, 0), geo.size.width)
                                let progress = geo.size.width > 0 ? Double(x / geo.size.width) : 0
                                player.seek(toProgress: progress)
                            }
                    )
                }
                .frame(height: 6)

                HStack {
                    Text(currentTimeLabel)
                    Spacer(minLength: 0)
                    Text(totalTimeLabel)
                }
                .font(.vazir(7.5, .bold))
                .foregroundStyle(Palette.primary.opacity(0.6))
            }
            .frame(maxWidth: .infinity)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Palette.surfaceLow, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .stroke(Palette.border, lineWidth: 1)
        )
        .environment(\.layoutDirection, .leftToRight)
    }

    private var artistGridSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("هنرمندان این برنامه")
                .font(.vazir(18, .bold))
                .foregroundStyle(Palette.primary)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.bottom, 12)
                .overlay(alignment: .bottom) {
                    Rectangle().fill(Color(hex: 0xE5E2DA)).frame(height: 1)
                }

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 16) {
                    ForEach(program.artists) { artist in
                        ArtistCard(
                            item: ArtistItem(
                                name: artist.name,
                                role: artist.role,
                                imageURL: artist.imageURL
                            ),
                            dark: false
                        )
                    }
                }
                .padding(.vertical, 4)
            }
        }
        .environment(\.layoutDirection, .rightToLeft)
    }

    private var timelineSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack(spacing: 20) {
                tabButton(title: "تایم‌لاین", tab: .timeline)
                tabButton(title: "اشعار", tab: .lyrics)

                Spacer(minLength: 0)
            }
            .environment(\.layoutDirection, .rightToLeft)
            .padding(.horizontal, 8)
            .overlay(alignment: .bottom) {
                Rectangle().fill(Color(hex: 0xE5E2DA)).frame(height: 1)
            }

            Group {
                switch selectedTab {
                case .timeline:
                    VStack(spacing: 14) {
                        ForEach(Array(program.timeline.enumerated()), id: \.element.id) { index, item in
                            ProgramTimelineRow(item: item, isActive: activeSegmentIndex == index)
                        }
                    }
                case .lyrics:
                    VStack(alignment: .leading, spacing: 12) {
                        ForEach(program.lyrics) { item in
                            ProgramLyricsRow(item: item)
                        }
                    }
                    .environment(\.layoutDirection, .rightToLeft)
                }
            }
            .environment(\.layoutDirection, .rightToLeft)
        }
    }

    private func tabButton(title: String, tab: ContentTab) -> some View {
        let isSelected = selectedTab == tab
        return Button {
            selectedTab = tab
        } label: {
            Text(title)
                .font(.vazir(15, .bold))
                .foregroundStyle(isSelected ? Palette.primary : Color(hex: 0x8F8E88))
                .padding(.bottom, 10)
                .overlay(alignment: .bottom) {
                    Rectangle()
                        .fill(isSelected ? Palette.secondary : .clear)
                        .frame(height: 3)
                }
        }
        .buttonStyle(.plain)
    }

    private var playbackTrack: TrackRowItem {
        TrackRowItem(
            id: program.programId.map { "program-\($0)" } ?? "program-\(program.id)",
            trackId: program.programId,
            title: program.title,
            subtitle: program.subtitle,
            duration: program.totalDuration,
            audioURL: program.audioURL,
            artworkURLs: program.artists.map { $0.imageURL }.filter { !$0.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty }
        )
    }

    private var isProgramActive: Bool {
        guard let current = player.currentTrack else { return false }
        if let currentId = current.trackId, let programId = program.programId {
            return currentId == programId
        }
        return current.id == playbackTrack.id
    }

    private var currentProgress: CGFloat {
        guard isProgramActive else { return 0 }
        return CGFloat(player.progress)
    }

    private var currentTimeLabel: String {
        if isProgramActive {
            return formatTime(player.currentTime)
        }
        return "00:00"
    }

    private var totalTimeLabel: String {
        if isProgramActive, player.duration > 0 {
            return formatTime(player.duration)
        }
        return normalizedDurationText(program.totalDuration)
    }

    private var activeSegmentIndex: Int? {
        guard isProgramActive, !program.timeline.isEmpty else { return nil }
        let starts = program.timeline.map { parseTimeToSeconds($0.time) }
        var best: Int? = nil
        for (index, start) in starts.enumerated() where player.currentTime >= start {
            best = index
        }
        return best
    }

    private func formatTime(_ seconds: Double) -> String {
        let safe = max(0, Int(seconds.rounded()))
        let mins = safe / 60
        let secs = safe % 60
        return String(format: "%02d:%02d", mins, secs)
    }

    private func normalizedDurationText(_ value: String) -> String {
        value
            .replacingOccurrences(of: " ", with: "")
            .replacingOccurrences(of: "۰", with: "0")
            .replacingOccurrences(of: "۱", with: "1")
            .replacingOccurrences(of: "۲", with: "2")
            .replacingOccurrences(of: "۳", with: "3")
            .replacingOccurrences(of: "۴", with: "4")
            .replacingOccurrences(of: "۵", with: "5")
            .replacingOccurrences(of: "۶", with: "6")
            .replacingOccurrences(of: "۷", with: "7")
            .replacingOccurrences(of: "۸", with: "8")
            .replacingOccurrences(of: "۹", with: "9")
    }

    private func parseTimeToSeconds(_ value: String) -> Double {
        let normalized = value
            .replacingOccurrences(of: " ", with: "")
            .replacingOccurrences(of: "۰", with: "0")
            .replacingOccurrences(of: "۱", with: "1")
            .replacingOccurrences(of: "۲", with: "2")
            .replacingOccurrences(of: "۳", with: "3")
            .replacingOccurrences(of: "۴", with: "4")
            .replacingOccurrences(of: "۵", with: "5")
            .replacingOccurrences(of: "۶", with: "6")
            .replacingOccurrences(of: "۷", with: "7")
            .replacingOccurrences(of: "۸", with: "8")
            .replacingOccurrences(of: "۹", with: "9")

        let parts = normalized.split(separator: ":").compactMap { Double($0) }
        if parts.count == 2 {
            return parts[0] * 60 + parts[1]
        }
        if parts.count == 3 {
            return parts[0] * 3600 + parts[1] * 60 + parts[2]
        }
        return 0
    }
}

private struct ProgramTimelineRow: View {
    let item: ProgramTimelineItem
    let isActive: Bool

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            VStack(spacing: 6) {
                Text(item.time)
                    .font(.vazir(13.5, .bold))
                    .foregroundStyle(Palette.secondary)

                Text(item.segmentTitle)
                    .font(.vazir(9.5, .medium))
                    .foregroundStyle(Palette.primary.opacity(0.8))
                    .lineLimit(2)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)

                if isActive {
                    HStack(spacing: 3) {
                        Rectangle().fill(Palette.secondary).frame(width: 2, height: 10)
                        Rectangle().fill(Palette.secondary).frame(width: 2, height: 15)
                        Rectangle().fill(Palette.secondary).frame(width: 2, height: 7)
                    }
                } else {
                    Image(systemName: "play.fill")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundStyle(Color(hex: 0xC0BDB4))
                }
            }
            .frame(width: 120)

            HStack(spacing: 18) {
                VStack(alignment: .trailing, spacing: 2) {
                    Text(item.segmentTitle)
                        .font(.vazir(13.5, .bold))
                        .foregroundStyle(Palette.primary)
                    Text("دستگاه")
                        .font(.vazir(7.5, .bold))
                        .foregroundStyle(Color(hex: 0x8F8E88))
                }
                .frame(width: 170, alignment: .leading)

                metaColumn(value: item.singer, label: "خواننده")
                metaColumn(value: item.musician, label: "نوازنده")
                metaColumn(value: item.poet, label: "شاعر")
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(isActive ? .white : Palette.surfaceLow, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .stroke(isActive ? Palette.secondary.opacity(0.3) : .clear, lineWidth: 1)
        )
        .overlay(alignment: .trailing) {
            if isActive {
                Rectangle()
                    .fill(Palette.secondary)
                    .frame(width: 6)
                    .clipShape(RoundedRectangle(cornerRadius: 3, style: .continuous))
            }
        }
    }

    private func metaColumn(value: String, label: String) -> some View {
        VStack(alignment: .leading, spacing: 1) {
            Text(value)
                .font(.vazir(10.5, .bold))
                .foregroundStyle(Color(hex: 0x44403C))
                .lineLimit(1)
            Text(label)
                .font(.vazir(7.5, .bold))
                .foregroundStyle(Color(hex: 0x8F8E88))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct ProgramLyricsRow: View {
    let item: ProgramLyricItem

    var body: some View {
        Text(item.text.replacingOccurrences(of: "\n", with: " / "))
            .font(.vazir(12))
            .foregroundStyle(Color(hex: 0x44403C))
            .multilineTextAlignment(.leading)
            .lineLimit(1)
            .truncationMode(.tail)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 20)
            .padding(.vertical, 10)
            .background(Palette.surfaceLow, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
            .environment(\.layoutDirection, .rightToLeft)
    }
}
