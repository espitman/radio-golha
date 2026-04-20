import SwiftUI

struct ProgramCard: View {
    let item: ProgramItem

    var body: some View {
        HStack(spacing: 16) {
            VStack(alignment: .trailing, spacing: 0) {
                Text(item.title)
                    .font(.vazir(13.5))
                    .foregroundStyle(Palette.primary)
                    .multilineTextAlignment(.trailing)
                Text(item.count)
                    .font(.vazir(9))
                    .foregroundStyle(Palette.textMuted)
            }

            Spacer(minLength: 0)

            RoundedRectangle(cornerRadius: 8, style: .continuous)
                .fill(Palette.primary.opacity(0.05))
                .frame(width: 64, height: 64)
                .overlay {
                    Image(systemName: item.symbol)
                        .foregroundStyle(Palette.secondary)
                }
        }
        .padding(.horizontal, 17)
        .frame(width: 214, height: 106)
        .background(Palette.surfaceLow, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(Palette.border, lineWidth: 1)
        )
    }
}
