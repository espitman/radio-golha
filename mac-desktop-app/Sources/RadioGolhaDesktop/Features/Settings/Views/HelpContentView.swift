import SwiftUI

struct HelpContentView: View {
    var body: some View {
        ZStack {
            Palette.surface

            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    Text("راهنما")
                        .font(.vazir(27, .bold))
                        .foregroundStyle(Palette.primary)

                    Text("این بخش به‌زودی با راهنمای کامل استفاده از اپ تکمیل می‌شود.")
                        .font(.vazir(10.5))
                        .foregroundStyle(Palette.textMuted)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                .padding(.horizontal, 48)
                .padding(.top, 32)
                .padding(.bottom, 24)
                .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
        .background(Palette.surface)
    }
}
