import SwiftUI
import AppKit

enum DesktopMainTab {
    case programs
    case artists
    case instrumentalists
    case modes
    case poets
}

struct DesktopTopNavigationBar: View {
    let selectedTab: DesktopMainTab
    var onSelectTab: (DesktopMainTab) -> Void

    var body: some View {
        HStack {
            HStack(spacing: 24) {
                Text("رادیو گلها")
                    .font(.vazir(15, .bold))
                    .foregroundStyle(Palette.primaryMuted)
                    .lineLimit(1)

                HStack(spacing: 20) {
                    navButton(title: "برنامه‌ها", tab: .programs)
                    navButton(title: "خواننده‌ها", tab: .artists)
                    navButton(title: "نوازندگان", tab: .instrumentalists)
                    navButton(title: "دستگاه‌ها", tab: .modes)
                    navButton(title: "شاعران", tab: .poets)
                }
            }

            Spacer(minLength: 0)

            HStack(spacing: 24) {
                HStack(spacing: 10) {
                    Spacer(minLength: 0)
                    Text("جستجو در آرشیو...")
                        .font(.vazir(9.75))
                        .foregroundStyle(Palette.text.opacity(0.55))
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundStyle(Palette.primary.opacity(0.45))
                }
                .padding(.horizontal, 12)
                .frame(width: 220, height: 36)
                .background(Palette.surfaceLow, in: RoundedRectangle(cornerRadius: 18, style: .continuous))

                Image(systemName: "person.crop.circle")
                    .font(.system(size: 18, weight: .regular))
                    .foregroundStyle(Palette.primary.opacity(0.6))
            }
        }
        .frame(height: 80)
        .padding(.horizontal, 48)
        .background(
            ZStack {
                WindowDragRegion()
                Palette.surface.opacity(0.82)
            }
        )
    }

    private func navButton(title: String, tab: DesktopMainTab) -> some View {
        let selected = (selectedTab == tab)
        return Button {
            onSelectTab(tab)
        } label: {
            VStack(spacing: 6) {
                Text(title)
                    .font(.vazir(10.5, selected ? .bold : .regular))
                    .foregroundStyle(selected ? Palette.primaryMuted : Palette.text.opacity(0.7))
                    .lineLimit(1)
                    .fixedSize(horizontal: true, vertical: false)
                Rectangle()
                    .fill(selected ? Palette.secondary : .clear)
                    .frame(width: 46, height: 2)
            }
        }
        .buttonStyle(.plain)
        .layoutPriority(1)
    }
}

private struct WindowDragRegion: NSViewRepresentable {
    func makeNSView(context: Context) -> NSView {
        DragRegionView()
    }

    func updateNSView(_ nsView: NSView, context: Context) {}
}

private final class DragRegionView: NSView {
    override var mouseDownCanMoveWindow: Bool { true }
}
