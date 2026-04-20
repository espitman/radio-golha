import SwiftUI

enum Palette {
    static let surface = Color(hex: 0xFCF9F0)
    static let surfaceLow = Color(hex: 0xF6F3EA)
    static let sidebar = Color(hex: 0xF1EEE5)
    static let primary = Color(hex: 0x002045)
    static let primaryMuted = Color(hex: 0x1A365D)
    static let secondary = Color(hex: 0x775A19)
    static let text = Color(hex: 0x1C1C17)
    static let textMuted = Color(hex: 0x78716C)
    static let border = Color(red: 196/255, green: 198/255, blue: 207/255, opacity: 0.20)
    static let darkSection = Color(hex: 0x202020)
}

extension Color {
    init(hex: Int, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: alpha
        )
    }
}

extension Font {
    static func vazir(_ size: CGFloat, _ weight: Weight = .regular) -> Font {
        // This machine has Vazirmatn Regular + Bold installed.
        // Map requested weight to the actual installed font files
        // to avoid synthetic metrics drift against Figma.
        let fontName: String
        switch weight {
        case .bold, .semibold, .heavy, .black:
            fontName = "Vazirmatn-Bold"
        default:
            fontName = "Vazirmatn-Regular"
        }
        return .custom(fontName, size: size)
    }
}
