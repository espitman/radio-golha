import SwiftUI

struct SidebarFooter: View {
    private let profileImage = "https://www.figma.com/api/mcp/asset/c0ff417a-1b3f-474a-bc8a-fb697ca6362e"

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            footerRow(title: "تنظیمات", symbol: "gearshape")
            footerRow(title: "راهنما", symbol: "questionmark.circle")

            HStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 0) {
                    Text("حساب متصدی")
                        .font(.vazir(9))
                        .foregroundStyle(Palette.primary)
                    Text("دسترسی به آرشیو")
                        .font(.vazir(7.5))
                        .foregroundStyle(Palette.text.opacity(0.5))
                }

                RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .fill(Color(hex: 0xE5E2DA))
                    .overlay {
                        FigmaAssetImage(url: profileImage, fallbackSymbol: "person.fill", fallbackTint: Palette.primary.opacity(0.7))
                    }
                    .frame(width: 40, height: 40)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.top, 16)
        }
        .padding(.horizontal, 32)
        .padding(.top, 33)
        .padding(.bottom, 28)
        .overlay(alignment: .top) {
            Rectangle()
                .fill(Color(red: 28/255, green: 28/255, blue: 23/255, opacity: 0.05))
                .frame(height: 1)
        }
    }

    private func footerRow(title: String, symbol: String) -> some View {
        HStack(spacing: 12) {
            Image(systemName: symbol)
                .font(.system(size: 12, weight: .regular))
                .foregroundStyle(Palette.text.opacity(0.7))
                .frame(width: 15, height: 15)
            Text(title)
                .font(.vazir(10.5))
                .foregroundStyle(Palette.text.opacity(0.7))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .multilineTextAlignment(.leading)
    }
}
