import SwiftUI

struct TopNavBarSection: View {
    var body: some View {
        HStack(spacing: 24) {
            HStack(spacing: 24) {
                Image(systemName: "person.crop.circle.badge.questionmark")
                    .font(.system(size: 14))
                    .foregroundStyle(Palette.primary.opacity(0.7))

                ZStack(alignment: .trailing) {
                    RoundedRectangle(cornerRadius: 12, style: .continuous)
                        .fill(Palette.surfaceLow)
                    HStack(spacing: 8) {
                        Text("جستجو در آرشیو...")
                            .font(.vazir(10.5))
                            .foregroundStyle(Color.gray)
                        Image(systemName: "magnifyingglass")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundStyle(Color.gray)
                    }
                    .padding(.horizontal, 12)
                }
                .frame(width: 256, height: 36)
            }

            Spacer(minLength: 24)

            HStack(spacing: 32) {
                Text("شاعران")
                Text("دستگاه‌ها")
                Text("هنرمندان")
                VStack(spacing: 6) {
                    Text("برنامه‌ها")
                        .foregroundStyle(Palette.primaryMuted)
                    Rectangle().fill(Palette.secondary).frame(width: 46, height: 2)
                }
                Text("رادیو گلها")
                    .font(.vazir(15))
                    .foregroundStyle(Palette.primaryMuted)
            }
            .font(.vazir(10.5))
            .foregroundStyle(Palette.text.opacity(0.7))
        }
        .padding(.horizontal, 48)
        .background(Palette.surface.opacity(0.85))
    }
}
