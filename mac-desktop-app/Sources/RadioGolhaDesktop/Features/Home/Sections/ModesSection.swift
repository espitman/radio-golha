import SwiftUI

struct ModesSection: View {
    var body: some View {
        VStack(alignment: .trailing, spacing: 24) {
            Text("دستگاه‌ها و آوازها")
                .font(.vazir(24))
                .foregroundStyle(Color(hex: 0x003F8D))
                .frame(maxWidth: .infinity, alignment: .trailing)

            HStack(spacing: 16) {
                ForEach(HomeMockData.modes) { mode in
                    Text(mode.title)
                        .font(.vazir(10.5))
                        .foregroundStyle(Palette.text)
                        .padding(.horizontal, 18)
                        .padding(.vertical, 8)
                        .background(Palette.surface, in: RoundedRectangle(cornerRadius: 8, style: .continuous))
                }
                Spacer(minLength: 0)
            }
        }
    }
}
