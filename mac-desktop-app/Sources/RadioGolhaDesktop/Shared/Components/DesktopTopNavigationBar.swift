import SwiftUI
import AppKit

enum DesktopMainTab {
    case programs
    case search
    case artists
    case instrumentalists
    case modes
    case poets
}

struct DesktopTopNavigationBar: View {
    let selectedTab: DesktopMainTab?
    var canGoBack: Bool = false
    var onBack: () -> Void = {}
    var onSelectTab: (DesktopMainTab) -> Void
    var onOpenQuickArtist: (_ artistId: Int64, _ name: String, _ avatar: String?) -> Void = { _, _, _ in }
    var onOpenQuickTrack: (_ trackId: Int64, _ title: String) -> Void = { _, _ in }
    @State private var quickSearchText: String = ""
    @State private var quickSearchResults: [DesktopTopSearchResult] = []
    @State private var isQuickSearchLoading = false
    @State private var isQuickSearchFocused = false
    @State private var isQuickSearchPresented = false
    @State private var highlightedQuickResultIndex: Int? = nil
    @State private var quickSearchTask: Task<Void, Never>? = nil

    var body: some View {
        HStack {
            HStack(spacing: 24) {
                Text("رادیو گلها")
                    .font(.vazir(15, .bold))
                    .foregroundStyle(Palette.primaryMuted)
                    .lineLimit(1)

                HStack(spacing: 20) {
                    navButton(title: "صفحه اصلی", tab: .programs)
                    navButton(title: "خواننده‌ها", tab: .artists)
                    navButton(title: "نوازندگان", tab: .instrumentalists)
                    navButton(title: "جستجو", tab: .search)
                }
            }

            Spacer(minLength: 0)

            HStack(spacing: 24) {
                quickSearchField

                Button {
                    onBack()
                } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundStyle(Palette.primary.opacity(canGoBack ? 0.7 : 0.3))
                        .frame(width: 30, height: 30)
                        .background(Palette.surfaceLow, in: Circle())
                }
                .buttonStyle(.plain)
                .disabled(!canGoBack)
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
        .zIndex(1_000)
        .onChange(of: quickSearchText) { value in
            let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
            quickSearchTask?.cancel()
            guard !trimmed.isEmpty else {
                isQuickSearchLoading = false
                quickSearchResults = []
                highlightedQuickResultIndex = nil
                isQuickSearchPresented = false
                return
            }

            if isQuickSearchFocused {
                isQuickSearchPresented = true
            }
            isQuickSearchLoading = true
            quickSearchTask = Task {
                try? await Task.sleep(nanoseconds: 250_000_000)
                guard !Task.isCancelled else { return }
                let results = await DesktopTopSearchDataLoader.search(query: trimmed, limit: 10)
                guard !Task.isCancelled else { return }
                await MainActor.run {
                    guard quickSearchText.trimmingCharacters(in: .whitespacesAndNewlines) == trimmed else { return }
                    quickSearchResults = results
                    isQuickSearchLoading = false
                    highlightedQuickResultIndex = results.isEmpty ? nil : 0
                }
            }
        }
        .onChange(of: quickSearchResults.count) { count in
            if count == 0 {
                highlightedQuickResultIndex = nil
            } else if highlightedQuickResultIndex == nil || (highlightedQuickResultIndex ?? 0) >= count {
                highlightedQuickResultIndex = 0
            }
        }
        .onDisappear {
            quickSearchTask?.cancel()
            quickSearchTask = nil
        }
    }

    private var quickSearchField: some View {
        HStack(spacing: 8) {
            TopBarKeyAwareTextField(
                text: $quickSearchText,
                placeholder: "جستجو در آرشیو...",
                onFocusChange: { focused in
                    isQuickSearchFocused = focused
                    if focused && !quickSearchText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                        isQuickSearchPresented = true
                    }
                },
                onDownArrow: {
                    moveQuickSelection(1)
                },
                onUpArrow: {
                    moveQuickSelection(-1)
                },
                onEnter: {
                    selectQuickHighlightedOrFirst()
                },
                onEscape: {
                    isQuickSearchPresented = false
                }
            )
            .font(.vazir(9.75))
            .foregroundStyle(Palette.primaryMuted)
            .frame(maxWidth: .infinity, alignment: .trailing)

            Image(systemName: "magnifyingglass")
                .font(.system(size: 12, weight: .semibold))
                .foregroundStyle(Palette.primary.opacity(0.45))
        }
        .padding(.horizontal, 12)
        .frame(width: 260, height: 36)
        .background(Palette.surfaceLow, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
        .environment(\.layoutDirection, .rightToLeft)
        .overlay(alignment: .topTrailing) {
            if isQuickSearchPresented {
                quickSearchDropdown
                    .frame(width: 360)
                    .offset(y: 44)
                    .zIndex(1_001)
            }
        }
    }

    private var quickSearchDropdown: some View {
        VStack(spacing: 0) {
            if isQuickSearchLoading {
                HStack(spacing: 8) {
                    LoadingSpinner(color: Palette.secondary, size: 14, lineWidth: 2)
                    Text("در حال جستجو...")
                        .font(.vazir(9.5))
                        .foregroundStyle(Palette.textMuted)
                }
                .frame(maxWidth: .infinity, alignment: .trailing)
                .padding(.horizontal, 12)
                .frame(height: 44)
            } else if quickSearchResults.isEmpty {
                Text("نتیجه‌ای پیدا نشد")
                    .font(.vazir(9.5))
                    .foregroundStyle(Palette.textMuted)
                    .frame(maxWidth: .infinity, alignment: .trailing)
                    .padding(.horizontal, 12)
                    .frame(height: 44)
            } else {
                ForEach(Array(quickSearchResults.enumerated()), id: \.element.identity) { index, result in
                    Button {
                        selectQuickResult(result)
                    } label: {
                        HStack(spacing: 10) {
                            Spacer(minLength: 0)
                            VStack(alignment: .trailing, spacing: 2) {
                                Text(result.title)
                                    .font(.vazir(10, .bold))
                                    .foregroundStyle(Palette.primaryMuted)
                                    .lineLimit(1)
                                    .multilineTextAlignment(.trailing)
                                Text(result.subtitle)
                                    .font(.vazir(8.75))
                                    .foregroundStyle(Palette.textMuted)
                                    .lineLimit(1)
                                    .multilineTextAlignment(.trailing)
                            }
                            .layoutPriority(1)

                            if result.kind == .artist {
                                artistAvatar(result.avatar)
                            } else {
                                Image(systemName: "music.note")
                                    .font(.system(size: 12, weight: .semibold))
                                    .foregroundStyle(Palette.primary.opacity(0.72))
                                    .frame(width: 24, height: 24)
                                    .background(Palette.primary.opacity(0.08), in: Circle())
                            }
                        }
                        .padding(.horizontal, 12)
                        .frame(height: 48)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                        .background(
                            RoundedRectangle(cornerRadius: 8, style: .continuous)
                                .fill(index == highlightedQuickResultIndex ? Palette.primary.opacity(0.08) : .clear)
                        )
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                    if index < quickSearchResults.count - 1 {
                        Divider().overlay(Palette.border)
                    }
                }
            }
        }
        .environment(\.layoutDirection, .leftToRight)
        .background(.white, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .stroke(Palette.border, lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.08), radius: 16, x: 0, y: 8)
    }

    @ViewBuilder
    private func artistAvatar(_ url: String?) -> some View {
        if let url, let parsed = URL(string: url), !url.isEmpty {
            AsyncImage(url: parsed) { image in
                image.resizable().scaledToFill()
            } placeholder: {
                Color(hex: 0xEAE9E5)
            }
            .frame(width: 24, height: 24)
            .clipShape(Circle())
        } else {
            Image(systemName: "person.crop.circle.fill")
                .font(.system(size: 15))
                .foregroundStyle(Palette.primary.opacity(0.55))
                .frame(width: 24, height: 24)
        }
    }

    private func selectQuickResult(_ result: DesktopTopSearchResult) {
        switch result.kind {
        case .artist:
            onOpenQuickArtist(result.id, result.title, result.avatar)
        case .track:
            onOpenQuickTrack(result.trackId ?? result.id, result.title)
        }
        quickSearchText = ""
        quickSearchResults = []
        highlightedQuickResultIndex = nil
        isQuickSearchPresented = false
    }

    private func moveQuickSelection(_ step: Int) {
        guard !quickSearchResults.isEmpty else { return }
        if !isQuickSearchPresented {
            isQuickSearchPresented = true
        }
        let current = highlightedQuickResultIndex ?? (step > 0 ? -1 : quickSearchResults.count)
        let next = max(0, min(quickSearchResults.count - 1, current + step))
        highlightedQuickResultIndex = next
    }

    private func selectQuickHighlightedOrFirst() {
        guard !quickSearchResults.isEmpty else { return }
        let index = max(0, min(quickSearchResults.count - 1, highlightedQuickResultIndex ?? 0))
        selectQuickResult(quickSearchResults[index])
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

private struct TopBarKeyAwareTextField: NSViewRepresentable {
    @Binding var text: String
    var placeholder: String
    var onFocusChange: (Bool) -> Void
    var onDownArrow: () -> Void
    var onUpArrow: () -> Void
    var onEnter: () -> Void
    var onEscape: () -> Void

    func makeCoordinator() -> Coordinator {
        Coordinator(parent: self)
    }

    func makeNSView(context: Context) -> NSTextField {
        let field = ClickToFocusTextField()
        field.isBordered = false
        field.isBezeled = false
        field.drawsBackground = false
        field.focusRingType = .none
        field.alignment = .right
        field.font = NSFont(name: "Vazirmatn-Regular", size: 10.5) ?? NSFont.systemFont(ofSize: 10.5, weight: .regular)
        field.textColor = NSColor(Palette.primaryMuted)
        field.delegate = context.coordinator
        let paragraph = NSMutableParagraphStyle()
        paragraph.alignment = .right
        paragraph.baseWritingDirection = .rightToLeft
        field.placeholderAttributedString = NSAttributedString(
            string: placeholder,
            attributes: [
                .foregroundColor: NSColor(Color.gray.opacity(0.75)),
                .font: NSFont(name: "Vazirmatn-Regular", size: 10.0) ?? NSFont.systemFont(ofSize: 10.0, weight: .regular),
                .paragraphStyle: paragraph
            ]
        )
        return field
    }

    func updateNSView(_ nsView: NSTextField, context: Context) {
        nsView.alignment = .right
        nsView.font = NSFont(name: "Vazirmatn-Regular", size: 10.5) ?? NSFont.systemFont(ofSize: 10.5, weight: .regular)
        if nsView.stringValue != text {
            nsView.stringValue = text
        }
        let paragraph = NSMutableParagraphStyle()
        paragraph.alignment = .right
        paragraph.baseWritingDirection = .rightToLeft
        nsView.placeholderAttributedString = NSAttributedString(
            string: placeholder,
            attributes: [
                .foregroundColor: NSColor(Color.gray.opacity(0.75)),
                .font: NSFont(name: "Vazirmatn-Regular", size: 10.0) ?? NSFont.systemFont(ofSize: 10.0, weight: .regular),
                .paragraphStyle: paragraph
            ]
        )
        context.coordinator.parent = self
    }

    final class Coordinator: NSObject, NSTextFieldDelegate {
        var parent: TopBarKeyAwareTextField

        init(parent: TopBarKeyAwareTextField) {
            self.parent = parent
        }

        func controlTextDidBeginEditing(_ obj: Notification) {
            parent.onFocusChange(true)
            guard let field = obj.object as? NSTextField, let editor = field.currentEditor() else { return }
            editor.alignment = .right
            editor.baseWritingDirection = .rightToLeft
            editor.font = NSFont(name: "Vazirmatn-Regular", size: 10.5) ?? NSFont.systemFont(ofSize: 10.5, weight: .regular)
        }

        func controlTextDidEndEditing(_ obj: Notification) {
            parent.onFocusChange(false)
        }

        func controlTextDidChange(_ obj: Notification) {
            guard let field = obj.object as? NSTextField else { return }
            if let editor = field.currentEditor() {
                editor.alignment = .right
                editor.baseWritingDirection = .rightToLeft
                editor.font = NSFont(name: "Vazirmatn-Regular", size: 10.5) ?? NSFont.systemFont(ofSize: 10.5, weight: .regular)
            }
            parent.text = field.stringValue
        }

        func control(_ control: NSControl, textView: NSTextView, doCommandBy commandSelector: Selector) -> Bool {
            if commandSelector == #selector(NSResponder.moveDown(_:)) {
                parent.onDownArrow()
                return true
            }
            if commandSelector == #selector(NSResponder.moveUp(_:)) {
                parent.onUpArrow()
                return true
            }
            if commandSelector == #selector(NSResponder.cancelOperation(_:)) {
                parent.onEscape()
                return true
            }
            if commandSelector == #selector(NSResponder.insertNewline(_:)) {
                parent.onEnter()
                return true
            }
            return false
        }
    }
}

private final class ClickToFocusTextField: NSTextField {
    override func becomeFirstResponder() -> Bool {
        if let event = NSApp.currentEvent {
            switch event.type {
            case .leftMouseDown, .leftMouseUp, .rightMouseDown, .rightMouseUp:
                return super.becomeFirstResponder()
            default:
                return false
            }
        }
        return false
    }
}
