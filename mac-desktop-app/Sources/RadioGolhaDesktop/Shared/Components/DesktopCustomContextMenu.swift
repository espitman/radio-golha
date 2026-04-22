import SwiftUI
import AppKit

struct DesktopContextMenuAction: Identifiable {
    enum ActionRole {
        case normal
        case destructive
    }

    let id = UUID()
    let title: String
    let systemImage: String
    let isChecked: Bool
    let role: ActionRole
    let action: () -> Void

    init(
        title: String,
        systemImage: String,
        isChecked: Bool = false,
        role: ActionRole = .normal,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.systemImage = systemImage
        self.isChecked = isChecked
        self.role = role
        self.action = action
    }
}

extension View {
    func desktopCustomContextMenu(
        actions: [DesktopContextMenuAction],
        alignment: Alignment = .topTrailing
    ) -> some View {
        modifier(DesktopCustomContextMenuModifier(actions: actions, alignment: alignment))
    }
}

private struct DesktopCustomContextMenuModifier: ViewModifier {
    let actions: [DesktopContextMenuAction]
    let alignment: Alignment

    @State private var isPresented = false
    @State private var clickLocation: CGPoint = .zero
    @State private var eventMonitor: Any?

    private let openAnimation = Animation.easeOut(duration: 0.18)
    private let closeAnimation = Animation.easeIn(duration: 0.13)

    func body(content: Content) -> some View {
        content
            .overlay(
                SecondaryClickCaptureView { point in
                    guard !actions.isEmpty else { return }
                    clickLocation = point
                    withAnimation(openAnimation) {
                        isPresented = true
                    }
                }
            )
            .overlay {
                GeometryReader { geo in
                    if isPresented, !actions.isEmpty {
                        let menuWidth: CGFloat = 212
                        let menuHeight: CGFloat = menuHeightForActions(actions.count)
                        let x = min(
                            max(clickLocation.x, 8),
                            max(8, geo.size.width - menuWidth - 8)
                        )
                        let y = min(
                            max(clickLocation.y + 6, 8),
                            max(8, geo.size.height - menuHeight - 8)
                        )

                        DesktopCustomContextMenuCard(actions: actions) {
                            withAnimation(closeAnimation) {
                                isPresented = false
                            }
                        }
                        .frame(width: menuWidth)
                        .offset(x: x, y: y)
                        .transition(.opacity.combined(with: .scale(scale: 0.96, anchor: .topTrailing)))
                        .zIndex(20_000)
                    }
                }
            }
            .onChange(of: isPresented) { presented in
                if presented {
                    installEventMonitor()
                } else {
                    removeEventMonitor()
                }
            }
            .onDisappear {
                removeEventMonitor()
            }
            .zIndex(isPresented ? 30_000 : 0)
    }

    private func menuHeightForActions(_ count: Int) -> CGFloat {
        guard count > 0 else { return 44 }
        let rows = CGFloat(count)
        return (rows * 30) + (CGFloat(max(0, count - 1)) * 4) + 12
    }

    private func installEventMonitor() {
        guard eventMonitor == nil else { return }
        eventMonitor = NSEvent.addLocalMonitorForEvents(
            matching: [.leftMouseDown, .rightMouseDown, .otherMouseDown, .keyDown]
        ) { event in
            if event.type == .keyDown, event.keyCode != 53 {
                return event
            }
            withAnimation(closeAnimation) {
                isPresented = false
            }
            return event
        }
    }

    private func removeEventMonitor() {
        guard let eventMonitor else { return }
        NSEvent.removeMonitor(eventMonitor)
        self.eventMonitor = nil
    }
}

private struct DesktopCustomContextMenuCard: View {
    let actions: [DesktopContextMenuAction]
    let onClose: () -> Void

    var body: some View {
        VStack(spacing: 4) {
            ForEach(actions) { item in
                Button {
                    onClose()
                    item.action()
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: item.systemImage)
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundStyle(item.role == .destructive ? Color(hex: 0xA73333) : Palette.primary)
                            .frame(width: 14)

                        Text(item.title)
                            .font(.vazir(9.5, .bold))
                            .foregroundStyle(item.role == .destructive ? Color(hex: 0xA73333) : Palette.primary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .multilineTextAlignment(.leading)

                        Image(systemName: "checkmark")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundStyle(Palette.secondary)
                            .opacity(item.isChecked ? 1 : 0)
                            .frame(width: 12)
                    }
                    .padding(.horizontal, 10)
                    .frame(height: 30)
                    .background(
                        RoundedRectangle(cornerRadius: 8, style: .continuous)
                            .fill(Palette.primary.opacity(0.06))
                            .opacity(0.0)
                    )
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .environment(\.layoutDirection, .rightToLeft)
            }
        }
        .padding(6)
        .frame(width: 212)
        .background(Color.white, in: RoundedRectangle(cornerRadius: 10, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 10, style: .continuous)
                .stroke(Palette.border, lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.14), radius: 18, x: 0, y: 8)
    }
}

private struct SecondaryClickCaptureView: NSViewRepresentable {
    let onSecondaryClick: (CGPoint) -> Void

    func makeNSView(context: Context) -> SecondaryClickPassthroughNSView {
        let view = SecondaryClickPassthroughNSView(frame: .zero)
        view.onSecondaryClick = onSecondaryClick
        return view
    }

    func updateNSView(_ nsView: SecondaryClickPassthroughNSView, context: Context) {
        nsView.onSecondaryClick = onSecondaryClick
    }
}

private final class SecondaryClickPassthroughNSView: NSView {
    var onSecondaryClick: ((CGPoint) -> Void)?

    override var isFlipped: Bool { true }

    override func hitTest(_ point: NSPoint) -> NSView? {
        guard let event = NSApp.currentEvent else { return nil }
        switch event.type {
        case .rightMouseDown, .rightMouseUp, .otherMouseDown, .otherMouseUp:
            return self
        default:
            return nil
        }
    }

    override func rightMouseDown(with event: NSEvent) {
        onSecondaryClick?(convert(event.locationInWindow, from: nil))
    }

    override func otherMouseDown(with event: NSEvent) {
        onSecondaryClick?(convert(event.locationInWindow, from: nil))
    }

    override func mouseDown(with event: NSEvent) {
        // Support Control+Click as secondary click on macOS.
        if event.modifierFlags.contains(.control) {
            onSecondaryClick?(convert(event.locationInWindow, from: nil))
        } else {
            super.mouseDown(with: event)
        }
    }
}
