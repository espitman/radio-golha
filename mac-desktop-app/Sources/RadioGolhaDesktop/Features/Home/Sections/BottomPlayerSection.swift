import SwiftUI
import Combine

struct BottomPlayerSection: View {
    @ObservedObject var player: DesktopAudioPlayer

    var body: some View {
        ZStack(alignment: .top) {
            Palette.primary
                .shadow(color: Color(red: 28/255, green: 28/255, blue: 23/255, opacity: 0.08), radius: 20, x: 0, y: -4)

            ZStack {
                HStack(spacing: 0) {
                    HStack(spacing: 16) {
                        EqualizerIndicator(isActive: player.isPlaying)
                            .frame(width: 44, height: 32)

                        Text(timeLabel)
                            .font(.vazir(12, .bold))
                            .foregroundStyle(.white.opacity(0.9))
                            .padding(.leading, 25)
                            .overlay(alignment: .leading) {
                                Rectangle().fill(Color.white.opacity(0.1)).frame(width: 1)
                            }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)

                    Spacer(minLength: 0)

                    HStack(spacing: 16) {
                        VStack(alignment: .trailing, spacing: 0) {
                            Text(player.currentTrack?.title ?? "—")
                                .font(.vazir(12))
                                .foregroundStyle(.white)
                                .lineLimit(1)
                                .truncationMode(.tail)
                            Text(player.currentTrack?.subtitle ?? "—")
                                .font(.vazir(9))
                                .foregroundStyle(.white.opacity(0.6))
                                .lineLimit(1)
                                .truncationMode(.tail)
                        }
                        .frame(width: 250, alignment: .trailing)

                        RoundedRectangle(cornerRadius: 2, style: .continuous)
                            .fill(Color.white.opacity(0.1))
                            .overlay {
                                RotatingTrackArtworkView(
                                    urls: player.currentTrack?.artworkURLs ?? []
                                )
                            }
                            .frame(width: 48, height: 48)
                    }
                    .frame(maxWidth: .infinity, alignment: .trailing)
                }

                HStack(spacing: 24) {
                    playerSymbol("shuffle")
                        .frame(width: 18, height: 20)
                        .opacity(0.35)
                    playerSymbol("backward.end.fill")
                        .frame(width: 16.25, height: 15)
                        .opacity(0.35)
                    Button {
                        player.togglePlayPause()
                    } label: {
                        ZStack {
                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                                .fill(.white)
                                .frame(width: 48, height: 48)
                            if player.isLoading {
                                LoadingSpinner(color: Palette.primary, size: 16, lineWidth: 2.4)
                            } else {
                                playerSymbol(player.isPlaying ? "pause.fill" : "play.fill", tint: Palette.primary)
                                    .frame(width: 13.75, height: 17.5)
                            }
                        }
                    }
                    .buttonStyle(.plain)
                    playerSymbol("forward.end.fill")
                        .frame(width: 16.25, height: 15)
                        .opacity(0.35)
                    playerSymbol("repeat")
                        .frame(width: 16, height: 16)
                        .opacity(0.35)
                }
                .frame(width: 320, alignment: .center)
            }
            .frame(width: 1184, height: 96)
            .padding(.horizontal, 48)
            .environment(\.layoutDirection, .leftToRight)

            seekBar
        }
        .frame(width: 1280, height: 96)
    }

    private func playerSymbol(_ name: String, tint: Color = .white) -> some View {
        Image(systemName: name)
            .resizable()
            .scaledToFit()
            .foregroundStyle(tint)
    }

    private var timeLabel: String {
        let elapsed = formatTime(player.currentTime)
        let total: String = {
            if player.duration > 0 {
                return formatTime(player.duration)
            }
            return normalizedDurationText(player.currentTrack?.duration ?? "00:00")
        }()
        return "\(elapsed) / \(total)"
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

    private var seekBar: some View {
        GeometryReader { geo in
            let barWidth = geo.size.width
            let progress = CGFloat(player.progress)

            ZStack(alignment: .leading) {
                Rectangle()
                    .fill(.white.opacity(0.1))
                    .frame(height: 6)

                Rectangle()
                    .fill(Color(hex: 0xD4AF37))
                    .frame(width: max(0, min(barWidth, barWidth * progress)), height: 6)
            }
            .contentShape(Rectangle())
            .gesture(
                DragGesture(minimumDistance: 0)
                    .onChanged { value in
                        guard barWidth > 0 else { return }
                        let x = min(max(value.location.x, 0), barWidth)
                        let seekProgress = Double(x / barWidth)
                        player.seek(toProgress: seekProgress)
                    }
            )
        }
        .frame(width: 1280, height: 8)
        .frame(maxWidth: .infinity, alignment: .top)
        .environment(\.layoutDirection, .leftToRight)
    }
}

private struct RotatingTrackArtworkView: View {
    let urls: [String]
    @State private var index: Int = 0

    private let timer = Timer.publish(every: 3, on: .main, in: .common).autoconnect()

    private var displayURLs: [String] {
        urls.filter { !$0.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty }
    }

    var body: some View {
        ZStack {
            if displayURLs.isEmpty {
                Image(systemName: "music.note")
                    .resizable()
                    .scaledToFit()
                    .foregroundStyle(.white.opacity(0.85))
                    .padding(10)
            } else {
                FigmaAssetImage(
                    url: displayURLs[index % displayURLs.count],
                    fallbackSymbol: "music.note",
                    fallbackTint: .white.opacity(0.85)
                )
                .id(displayURLs[index % displayURLs.count])
                .transition(.opacity)
            }
        }
        .animation(.easeInOut(duration: 0.45), value: index)
        .onReceive(timer) { _ in
            guard displayURLs.count > 1 else { return }
            index = (index + 1) % displayURLs.count
        }
        .onChange(of: displayURLs) { _ in
            index = 0
        }
    }
}

private struct EqualizerIndicator: View {
    let isActive: Bool
    @State private var tick: Int = 0
    private let timer = Timer.publish(every: 0.16, on: .main, in: .common).autoconnect()
    private let base: [CGFloat] = [8, 20, 32, 16, 24, 12]

    var body: some View {
        HStack(spacing: 4) {
            ForEach(Array(base.enumerated()), id: \.offset) { index, fallback in
                RoundedRectangle(cornerRadius: 12)
                    .fill((isActive ? Color(hex: 0xD4AF37) : Palette.secondary).opacity(isActive ? 1.0 : 0.35))
                    .frame(width: 4, height: barHeight(index: index, fallback: fallback))
            }
        }
        .animation(.easeInOut(duration: 0.32), value: tick)
        .onReceive(timer) { _ in
            if isActive {
                tick = (tick + 1) % 10_000
            }
        }
    }

    private func barHeight(index: Int, fallback: CGFloat) -> CGFloat {
        guard isActive else { return fallback }
        let phase = Double(tick) * 0.42 + Double(index) * 0.95
        let wave = (sin(phase) + 1) / 2 // 0...1
        return 10 + CGFloat(wave) * 18
    }
}
