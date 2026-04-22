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
    @State private var eventMonitor: Any?

    private let openAnimation = Animation.easeOut(duration: 0.18)
    private let closeAnimation = Animation.easeIn(duration: 0.13)

    func body(content: Content) -> some View {
        content
            .background(
                SecondaryClickCaptureView {
                    guard !actions.isEmpty else { return }
                    withAnimation(openAnimation) {
                        isPresented = true
                    }
                }
            )
            .overlay(alignment: alignment) {
                if isPresented, !actions.isEmpty {
                    DesktopCustomContextMenuCard(actions: actions) {
                        withAnimation(closeAnimation) {
                            isPresented = false
                        }
                    }
                    .padding(.top, 8)
                    .transition(.opacity.combined(with: .scale(scale: 0.96, anchor: .topTrailing)))
                    .zIndex(20_000)
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
        .background(Palette.surface, in: RoundedRectangle(cornerRadius: 10, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 10, style: .continuous)
                .stroke(Palette.border, lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.14), radius: 18, x: 0, y: 8)
    }
}

private struct SecondaryClickCaptureView: NSViewRepresentable {
    let onSecondaryClick: () -> Void

    func makeCoordinator() -> Coordinator {
        Coordinator(onSecondaryClick: onSecondaryClick)
    }

    func makeNSView(context: Context) -> NSView {
        let view = NSView(frame: .zero)
        return view
    }

    func updateNSView(_ nsView: NSView, context: Context) {
        context.coordinator.onSecondaryClick = onSecondaryClick
        context.coordinator.bind(to: nsView.superview)
    }

    static func dismantleNSView(_ nsView: NSView, coordinator: Coordinator) {
        coordinator.unbind()
    }

    final class Coordinator: NSObject {
        var onSecondaryClick: () -> Void
        private weak var hostView: NSView?
        private var recognizer: NSClickGestureRecognizer?

        init(onSecondaryClick: @escaping () -> Void) {
            self.onSecondaryClick = onSecondaryClick
        }

        func bind(to view: NSView?) {
            guard let view else { return }
            if hostView === view, recognizer != nil { return }
            unbind()
            hostView = view

            let recognizer = NSClickGestureRecognizer(target: self, action: #selector(handleSecondaryClick(_:)))
            recognizer.buttonMask = 0x2
            recognizer.numberOfClicksRequired = 1
            recognizer.delaysPrimaryMouseButtonEvents = false
            view.addGestureRecognizer(recognizer)
            self.recognizer = recognizer
        }

        func unbind() {
            if let recognizer, let hostView {
                hostView.removeGestureRecognizer(recognizer)
            }
            recognizer = nil
        }

        @objc private func handleSecondaryClick(_ sender: NSClickGestureRecognizer) {
            guard sender.state == .ended else { return }
            onSecondaryClick()
        }
    }
}
