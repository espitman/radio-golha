import SwiftUI
import AppKit

struct DesktopRTLTextField: NSViewRepresentable {
    @Binding var text: String
    let placeholder: String
    var placeholderColor: NSColor = NSColor(calibratedWhite: 0.56, alpha: 1.0)
    var textColor: NSColor = .black
    var isFirstResponder: Bool = false
    var onSubmit: () -> Void = {}

    private var vazirFont: NSFont {
        NSFont(name: "Vazirmatn-Regular", size: 13) ?? NSFont.systemFont(ofSize: 13, weight: .regular)
    }

    private var rtlParagraphStyle: NSParagraphStyle {
        let style = NSMutableParagraphStyle()
        style.alignment = .right
        style.baseWritingDirection = .rightToLeft
        style.lineBreakMode = .byTruncatingTail
        return style
    }

    private var placeholderAttributes: [NSAttributedString.Key: Any] {
        [
            .foregroundColor: placeholderColor,
            .font: vazirFont,
            .paragraphStyle: rtlParagraphStyle
        ]
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(parent: self)
    }

    func makeNSView(context: Context) -> NSTextField {
        let field = NSTextField(string: text)
        field.isBordered = false
        field.isBezeled = false
        field.drawsBackground = false
        field.focusRingType = .none
        field.lineBreakMode = .byTruncatingTail
        field.maximumNumberOfLines = 1
        field.alignment = .right
        field.baseWritingDirection = .rightToLeft
        field.font = vazirFont
        field.textColor = textColor
        field.delegate = context.coordinator
        field.placeholderAttributedString = NSAttributedString(
            string: placeholder,
            attributes: placeholderAttributes
        )
        return field
    }

    func updateNSView(_ nsView: NSTextField, context: Context) {
        context.coordinator.parent = self
        if nsView.stringValue != text {
            nsView.stringValue = text
        }
        nsView.alignment = .right
        nsView.baseWritingDirection = .rightToLeft
        nsView.font = vazirFont
        nsView.textColor = textColor
        nsView.placeholderAttributedString = NSAttributedString(
            string: placeholder,
            attributes: placeholderAttributes
        )

        if isFirstResponder, nsView.window?.firstResponder !== nsView.currentEditor() {
            DispatchQueue.main.async {
                nsView.window?.makeFirstResponder(nsView)
            }
        }
    }

    final class Coordinator: NSObject, NSTextFieldDelegate {
        var parent: DesktopRTLTextField

        init(parent: DesktopRTLTextField) {
            self.parent = parent
        }

        func controlTextDidChange(_ obj: Notification) {
            guard let field = obj.object as? NSTextField else { return }
            parent.text = field.stringValue
        }

        func controlTextDidBeginEditing(_ obj: Notification) {
            guard let editor = (obj.userInfo?["NSFieldEditor"] as? NSTextView) else { return }
            editor.alignment = .right
            editor.baseWritingDirection = .rightToLeft
            editor.font = parent.vazirFont
            editor.textColor = parent.textColor
        }

        func control(
            _ control: NSControl,
            textView: NSTextView,
            doCommandBy commandSelector: Selector
        ) -> Bool {
            if commandSelector == #selector(NSResponder.insertNewline(_:)) {
                parent.onSubmit()
                return true
            }
            return false
        }
    }
}
