import SwiftUI

struct SectionHeader: View {
    let title: String
    var dark: Bool = false

    var body: some View {
        HStack {
            HStack(spacing: 4) {
                Text("مشاهده همه")
                    .font(.vazir(12))
                Image(systemName: "arrow.left")
                    .font(.system(size: 10, weight: .medium))
            }
            .foregroundStyle(Palette.secondary)

            Spacer(minLength: 0)

            Text(title)
                .font(.vazir(22.5))
                .foregroundStyle(dark ? Color(hex: 0x003F8D) : Palette.primary)
        }
    }
}
