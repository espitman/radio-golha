import SwiftUI
#if os(macOS)
import AppKit
#endif

private enum SearchMatchModeUI {
    case all
    case any

    var label: String {
        switch self {
        case .all: return "همه"
        case .any: return "هرکدام"
        }
    }
}

private struct SearchOption: Identifiable, Hashable {
    let id: String
    let name: String
    let avatarURL: String?
}

private enum SearchFieldKey: String, CaseIterable, Hashable {
    case category
    case singer
    case mode
    case orchestra
    case instrument
    case performer
    case poet
    case announcer
    case composer
    case arranger
    case orchestraLeader

    var title: String {
        switch self {
        case .category: return "دسته برنامه"
        case .singer: return "خواننده"
        case .mode: return "دستگاه"
        case .orchestra: return "ارکستر"
        case .instrument: return "ساز"
        case .performer: return "نوازنده"
        case .poet: return "شاعر"
        case .announcer: return "گوینده"
        case .composer: return "آهنگساز"
        case .arranger: return "تنظیم‌کننده"
        case .orchestraLeader: return "رهبر ارکستر"
        }
    }

    var placeholder: String {
        switch self {
        case .category: return "گل‌های تازه، گل‌های رنگارنگ..."
        case .singer: return "نام خواننده..."
        case .mode: return "شور، ماهور، همایون..."
        case .orchestra: return "نام ارکستر..."
        case .instrument: return "تار، نی، پیانو..."
        case .performer: return "نام نوازنده..."
        case .poet: return "حافظ، سعدی، مولانا..."
        case .announcer: return "نام گوینده..."
        case .composer: return "نام آهنگساز..."
        case .arranger: return "نام تنظیم‌کننده..."
        case .orchestraLeader: return "نام رهبر ارکستر..."
        }
    }

    var supportsAvatar: Bool {
        switch self {
        case .singer, .performer, .poet, .announcer, .composer, .arranger, .orchestraLeader:
            return true
        default:
            return false
        }
    }
}

private struct SearchFieldConfig: Identifiable {
    let key: SearchFieldKey
    let options: [SearchOption]

    var id: String { key.rawValue }

    static let empty: [SearchFieldConfig] = SearchFieldKey.allCases.map { .init(key: $0, options: []) }

    static func fromLoaded(_ loaded: SearchLoadedOptions) -> [SearchFieldConfig] {
        func mapped(_ values: [SearchDataOption]) -> [SearchOption] {
            let options = values.map { SearchOption(id: $0.id, name: $0.name, avatarURL: $0.avatarURL) }
            var seen = Set<String>()
            return options.filter { option in
                let key = option.id + "|" + option.name
                return !option.name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && seen.insert(key).inserted
            }
        }

        return [
            .init(key: .category, options: mapped(loaded.categories)),
            .init(key: .singer, options: mapped(loaded.singers)),
            .init(key: .mode, options: mapped(loaded.modes)),
            .init(key: .orchestra, options: mapped(loaded.orchestras)),
            .init(key: .instrument, options: mapped(loaded.instruments)),
            .init(key: .performer, options: mapped(loaded.performers)),
            .init(key: .poet, options: mapped(loaded.poets)),
            .init(key: .announcer, options: mapped(loaded.announcers)),
            .init(key: .composer, options: mapped(loaded.composers)),
            .init(key: .arranger, options: mapped(loaded.arrangers)),
            .init(key: .orchestraLeader, options: mapped(loaded.orchestraLeaders))
        ]
    }
}

struct SearchContentView: View {
    @State private var fieldConfigs: [SearchFieldConfig]

    @State private var modeByField: [SearchFieldKey: SearchMatchModeUI]
    @State private var queryByField: [SearchFieldKey: String]
    @State private var selectedByField: [SearchFieldKey: [SearchOption]]
    @State private var expandedField: SearchFieldKey?
    @State private var transcriptText: String

    init() {
        _fieldConfigs = State(initialValue: SearchFieldConfig.empty)
        _modeByField = State(initialValue: Dictionary(uniqueKeysWithValues: SearchFieldKey.allCases.map { ($0, .all) }))
        _queryByField = State(initialValue: Dictionary(uniqueKeysWithValues: SearchFieldKey.allCases.map { ($0, "") }))
        _selectedByField = State(initialValue: Dictionary(uniqueKeysWithValues: SearchFieldKey.allCases.map { ($0, []) }))
        _expandedField = State(initialValue: nil)
        _transcriptText = State(initialValue: "")
    }

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                header
                    .padding(.horizontal, 48)
                    .padding(.top, 32)

                form
                    .padding(.horizontal, 48)
                    .padding(.top, 34)
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
        .environment(\.layoutDirection, .rightToLeft)
        .task {
            if let loaded = await SearchDataLoader.load() {
                fieldConfigs = SearchFieldConfig.fromLoaded(loaded)
            }
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("جستجوی پیشرفته در گنجینه")
                .font(.vazir(27, .bold))
                .foregroundStyle(Palette.primary)
                .frame(maxWidth: .infinity, alignment: .leading)

            Text("با استفاده از فیلترهای زیر، در میراث موسیقی اصیل ایران کاوش کنید")
                .font(.vazir(10.5))
                .foregroundStyle(Color(hex: 0x43474E).opacity(0.75))
                .frame(maxWidth: .infinity, alignment: .leading)
        }
    }

    private var form: some View {
        VStack(spacing: 0) {
            VStack(alignment: .leading, spacing: 40) {
                LazyVGrid(
                    columns: [
                        GridItem(.flexible(), spacing: 32),
                        GridItem(.flexible(), spacing: 32)
                    ],
                    spacing: 28
                ) {
                    ForEach(Array(fieldConfigs.enumerated()), id: \.element.id) { index, config in
                        SearchComboField(
                            key: config.key,
                            options: config.options,
                            mode: Binding(
                                get: { modeByField[config.key] ?? .all },
                                set: { modeByField[config.key] = $0 }
                            ),
                            query: Binding(
                                get: { queryByField[config.key, default: ""] },
                                set: { queryByField[config.key] = $0 }
                            ),
                            selected: Binding(
                                get: { selectedByField[config.key, default: []] },
                                set: { selectedByField[config.key] = $0 }
                            ),
                            isExpanded: Binding(
                                get: { expandedField == config.key },
                                set: { expanded in
                                    expandedField = expanded ? config.key : nil
                                }
                            )
                        )
                        .zIndex(expandedField == config.key ? 10_000 : Double(fieldConfigs.count - index))
                    }

                    SearchTranscriptField(text: $transcriptText)
                        .gridCellColumns(2)
                }
                .zIndex(expandedField == nil ? 0 : 10_000)

                Divider().overlay(Palette.text.opacity(0.06))
                    .zIndex(0)

                HStack {
                    Spacer(minLength: 0)
                    Button {
                    } label: {
                        HStack(spacing: 8) {
                            Image(systemName: "magnifyingglass")
                                .font(.system(size: 14, weight: .bold))
                            Text("جستجوی هوشمند در آرشیو")
                                .font(.vazir(11, .bold))
                        }
                        .foregroundStyle(.white)
                        .padding(.horizontal, 34)
                        .padding(.vertical, 12)
                        .background(Palette.primary, in: Capsule())
                    }
                    .buttonStyle(.plain)
                    Spacer(minLength: 0)
                }
                .zIndex(0)
            }
            .padding(.horizontal, 32)
            .padding(.vertical, 30)
        }
        .background {
            if expandedField != nil {
                Color.black.opacity(0.001)
                    .contentShape(Rectangle())
                    .onTapGesture {
                        expandedField = nil
                    }
            }
        }
        .background(Palette.sidebar.opacity(0.5), in: RoundedRectangle(cornerRadius: 14, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(Palette.secondary.opacity(0.12), lineWidth: 1)
        )
    }
}

private struct SearchComboField: View {
    let key: SearchFieldKey
    let options: [SearchOption]
    @Binding var mode: SearchMatchModeUI
    @Binding var query: String
    @Binding var selected: [SearchOption]
    @Binding var isExpanded: Bool
    @State private var highlightedIndex: Int? = nil
    private let inputRowHeight: CGFloat = 38
    private let dropdownGap: CGFloat = 6

    private var suggestions: [SearchOption] {
        let selectedIds = Set(selected.map(\.id))
        let base = options.filter { !selectedIds.contains($0.id) }
        let q = query.trimmingCharacters(in: .whitespacesAndNewlines)
        if q.isEmpty {
            return base
        }
        let normalizedQuery = normalizeSearch(q)
        return base.filter { option in
            normalizeSearch(option.name).hasPrefix(normalizedQuery)
        }
    }

    private func normalizeSearch(_ value: String) -> String {
        value
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: "ي", with: "ی")
            .replacingOccurrences(of: "ك", with: "ک")
            .folding(options: [.diacriticInsensitive, .caseInsensitive], locale: Locale(identifier: "fa_IR"))
    }

    private var comboBaseHeight: CGFloat {
        inputRowHeight
    }

    private var dropdownOffsetY: CGFloat {
        comboBaseHeight + dropdownGap
    }

    private var titleText: String {
        selected.isEmpty ? key.title : "\(key.title) (\(selected.count))"
    }

    private func selectOption(_ option: SearchOption) {
        selected.append(option)
        query = ""
        isExpanded = true
        highlightedIndex = suggestions.isEmpty ? nil : 0
    }

    private func moveHighlight(_ step: Int) {
        guard !suggestions.isEmpty else {
            highlightedIndex = nil
            return
        }
        let current = highlightedIndex ?? (step > 0 ? -1 : suggestions.count)
        let next = max(0, min(suggestions.count - 1, current + step))
        highlightedIndex = next
        isExpanded = true
    }

    private func selectHighlighted() {
        guard !suggestions.isEmpty else { return }
        let index = max(0, min(suggestions.count - 1, highlightedIndex ?? 0))
        selectOption(suggestions[index])
    }

    var body: some View {
        VStack(alignment: .trailing, spacing: 8) {
            HStack {
                Text(titleText)
                    .font(.vazir(10.5, .bold))
                    .foregroundStyle(Palette.primary)

                Spacer(minLength: 0)

                MatchModeToggle(mode: $mode)
            }

            VStack(spacing: 0) {
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 8, style: .continuous)
                        .fill(.white.opacity(0.55))
                        .overlay(
                            RoundedRectangle(cornerRadius: 8, style: .continuous)
                                .stroke(Color(hex: 0xC4C6CF), lineWidth: 1)
                        )
                        .frame(height: inputRowHeight)

                    HStack(spacing: 8) {
                        if !selected.isEmpty {
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 6) {
                                    ForEach(selected, id: \.id) { item in
                                        SelectedBadge(option: item, showAvatar: key.supportsAvatar) {
                                            selected.removeAll { $0.id == item.id }
                                        }
                                    }
                                }
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                        }

                        KeyAwareTextField(
                            text: $query,
                            onFocus: {
                                isExpanded = true
                                highlightedIndex = suggestions.isEmpty ? nil : 0
                            },
                            onDownArrow: {
                                moveHighlight(1)
                            },
                            onUpArrow: {
                                moveHighlight(-1)
                            },
                            onEnter: {
                                selectHighlighted()
                            },
                            onEscape: {
                                isExpanded = false
                                highlightedIndex = nil
                            }
                        )
                        .frame(maxWidth: .infinity, alignment: .trailing)
                        .onChange(of: query) { value in
                            if !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                                isExpanded = true
                            }
                            highlightedIndex = suggestions.isEmpty ? nil : 0
                        }

                        Button {
                            isExpanded.toggle()
                        } label: {
                            Image(systemName: "chevron.down")
                                .font(.system(size: 11, weight: .semibold))
                                .foregroundStyle(Color(hex: 0x43474E).opacity(0.55))
                                .rotationEffect(.degrees(isExpanded ? 180 : 0))
                        }
                        .buttonStyle(.plain)
                    }
                    .padding(.horizontal, 12)
                    .frame(height: inputRowHeight)

                    if query.isEmpty && selected.isEmpty {
                        Text(key.placeholder)
                            .font(.vazir(10.5))
                            .foregroundStyle(Color(hex: 0x43474E).opacity(0.6))
                            .multilineTextAlignment(.trailing)
                            .frame(maxWidth: .infinity, alignment: .trailing)
                            .environment(\.layoutDirection, .leftToRight)
                            .padding(.horizontal, 12)
                            .frame(height: inputRowHeight)
                            .allowsHitTesting(false)
                    }
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: comboBaseHeight)
            .overlay(alignment: .topLeading) {
                if isExpanded {
                    dropdownList
                        .offset(y: dropdownOffsetY)
                        .zIndex(2)
                }
            }
            .zIndex(isExpanded ? 100 : 0)
        }
        .onChange(of: isExpanded) { expanded in
            if expanded {
                highlightedIndex = suggestions.isEmpty ? nil : 0
            } else {
                highlightedIndex = nil
            }
        }
    }

    private var dropdownList: some View {
        let rowHeight: CGFloat = 38
        let visibleCount = max(1, min(suggestions.count, 5))
        let listHeight = CGFloat(visibleCount) * rowHeight

        return VStack(spacing: 0) {
            if suggestions.isEmpty {
                Text("موردی یافت نشد")
                    .font(.vazir(9.5))
                    .foregroundStyle(Palette.textMuted)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 10)
                    .frame(height: rowHeight)
            } else {
                ScrollView(showsIndicators: false) {
                    VStack(spacing: 0) {
                        ForEach(Array(suggestions.enumerated()), id: \.element.id) { index, option in
                            Button {
                                selectOption(option)
                            } label: {
                                HStack(spacing: 8) {
                                    if key.supportsAvatar {
                                        AvatarThumb(url: option.avatarURL)
                                    }
                                    Text(option.name)
                                        .font(.vazir(10))
                                        .foregroundStyle(Palette.text)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                }
                                .padding(.horizontal, 10)
                                .frame(height: rowHeight)
                                .contentShape(Rectangle())
                                .background(
                                    RoundedRectangle(cornerRadius: 0, style: .continuous)
                                        .fill(highlightedIndex == index ? Palette.primary.opacity(0.10) : .clear)
                                )
                            }
                            .buttonStyle(.plain)

                            if index < suggestions.count - 1 {
                                Divider().overlay(Palette.text.opacity(0.08))
                            }
                        }
                    }
                }
                .frame(height: listHeight)
            }
        }
        .background(.white.opacity(0.95), in: RoundedRectangle(cornerRadius: 8, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 8, style: .continuous)
                .stroke(Palette.border, lineWidth: 1)
        )
    }
}

#if os(macOS)
private struct KeyAwareTextField: NSViewRepresentable {
    @Binding var text: String
    var onFocus: () -> Void
    var onDownArrow: () -> Void
    var onUpArrow: () -> Void
    var onEnter: () -> Void
    var onEscape: () -> Void

    func makeNSView(context: Context) -> KeyAwareNSTextField {
        let field = KeyAwareNSTextField()
        field.isBordered = false
        field.drawsBackground = false
        field.focusRingType = .none
        field.usesSingleLineMode = true
        field.lineBreakMode = .byTruncatingTail
        field.alignment = .right
        field.font = NSFont(name: "Vazirmatn-Regular", size: 10.5) ?? NSFont.systemFont(ofSize: 10.5)
        field.textColor = NSColor(Palette.text)
        field.delegate = context.coordinator
        context.coordinator.onFocus = onFocus
        context.coordinator.onDownArrow = onDownArrow
        context.coordinator.onUpArrow = onUpArrow
        context.coordinator.onEnter = onEnter
        context.coordinator.onEscape = onEscape
        field.onFocus = { context.coordinator.onFocus?() }
        return field
    }

    func updateNSView(_ nsView: KeyAwareNSTextField, context: Context) {
        if nsView.stringValue != text {
            nsView.stringValue = text
        }
        context.coordinator.onFocus = onFocus
        context.coordinator.onDownArrow = onDownArrow
        context.coordinator.onUpArrow = onUpArrow
        context.coordinator.onEnter = onEnter
        context.coordinator.onEscape = onEscape
        nsView.onFocus = { context.coordinator.onFocus?() }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(text: $text)
    }

    final class Coordinator: NSObject, NSTextFieldDelegate {
        @Binding var text: String
        var onFocus: (() -> Void)?
        var onDownArrow: (() -> Void)?
        var onUpArrow: (() -> Void)?
        var onEnter: (() -> Void)?
        var onEscape: (() -> Void)?

        init(text: Binding<String>) {
            _text = text
        }

        func controlTextDidChange(_ obj: Notification) {
            guard let field = obj.object as? NSTextField else { return }
            text = field.stringValue
        }

        func control(_ control: NSControl, textView: NSTextView, doCommandBy commandSelector: Selector) -> Bool {
            switch commandSelector {
            case #selector(NSResponder.moveDown(_:)):
                onDownArrow?()
                return true
            case #selector(NSResponder.moveUp(_:)):
                onUpArrow?()
                return true
            case #selector(NSResponder.insertNewline(_:)):
                onEnter?()
                return true
            case #selector(NSResponder.cancelOperation(_:)):
                onEscape?()
                return true
            default:
                return false
            }
        }
    }
}

private final class KeyAwareNSTextField: NSTextField {
    var onFocus: (() -> Void)?

    override func becomeFirstResponder() -> Bool {
        let didBecome = super.becomeFirstResponder()
        if didBecome {
            onFocus?()
        }
        return didBecome
    }

    override func keyDown(with event: NSEvent) {
        super.keyDown(with: event)
    }
}
#endif

private struct MatchModeToggle: View {
    @Binding var mode: SearchMatchModeUI

    var body: some View {
        ZStack(alignment: mode == .all ? .leading : .trailing) {
            Capsule()
                .fill(Color(hex: 0xE5E2DA))
                .frame(width: 124, height: 28)

            Capsule()
                .fill(.white)
                .frame(width: 60, height: 24)
                .padding(.horizontal, 2)

            HStack(spacing: 0) {
                Button {
                    mode = .all
                } label: {
                    Text("همه")
                        .font(.vazir(9.75, mode == .all ? .bold : .regular))
                        .foregroundStyle(mode == .all ? Palette.text : Color(hex: 0x43474E))
                        .frame(width: 62, height: 28)
                }
                .buttonStyle(.plain)

                Button {
                    mode = .any
                } label: {
                    Text("هرکدام")
                        .font(.vazir(9.75, mode == .any ? .bold : .regular))
                        .foregroundStyle(mode == .any ? Palette.text : Color(hex: 0x43474E))
                        .frame(width: 62, height: 28)
                }
                .buttonStyle(.plain)
            }
        }
        .animation(.easeInOut(duration: 0.2), value: mode)
        .environment(\.layoutDirection, .leftToRight)
    }
}

private struct SelectedBadge: View {
    let option: SearchOption
    let showAvatar: Bool
    var onRemove: () -> Void

    var body: some View {
        HStack(spacing: 6) {
            if showAvatar {
                AvatarThumb(url: option.avatarURL)
            }
            Text(option.name)
                .font(.vazir(9.5, .bold))
                .foregroundStyle(Palette.primary)
                .multilineTextAlignment(.trailing)

            Button {
                onRemove()
            } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 9, weight: .bold))
                    .foregroundStyle(Palette.primary.opacity(0.75))
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 5)
        .background(Palette.primary.opacity(0.08), in: Capsule())
        .overlay(Capsule().stroke(Palette.primary.opacity(0.15), lineWidth: 1))
    }
}

private struct AvatarThumb: View {
    let url: String?

    var body: some View {
        ZStack {
            Circle().fill(Palette.surfaceLow)
            if let url, !url.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                FigmaAssetImage(url: url, fallbackSymbol: "person.fill", fallbackTint: Palette.primary.opacity(0.7))
                    .clipShape(Circle())
            } else {
                Image(systemName: "person.fill")
                    .font(.system(size: 8, weight: .bold))
                    .foregroundStyle(Palette.primary.opacity(0.7))
            }
        }
        .frame(width: 18, height: 18)
    }
}

private struct SearchTranscriptField: View {
    @Binding var text: String

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("متن برنامه")
                .font(.vazir(10.5, .bold))
                .foregroundStyle(Palette.primary)
                .frame(maxWidth: .infinity, alignment: .leading)

            ZStack(alignment: .leading) {
                if text.isEmpty {
                    Text("جستجوی کلمات در اشعار و توضیحات...")
                        .font(.vazir(10.5))
                        .foregroundStyle(Color(hex: 0x43474E).opacity(0.6))
                        .multilineTextAlignment(.leading)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 16)
                        .frame(height: 38)
                }

                TextField("", text: $text)
                    .textFieldStyle(.plain)
                    .font(.vazir(10.5))
                    .foregroundStyle(Palette.text)
                    .multilineTextAlignment(.leading)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 16)
                    .frame(height: 38)
            }
            .frame(maxWidth: .infinity, minHeight: 38, alignment: .leading)
            .background(.white.opacity(0.55), in: RoundedRectangle(cornerRadius: 8, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 8, style: .continuous)
                    .stroke(Color(hex: 0xC4C6CF), lineWidth: 1)
            )
        }
    }
}
