import SwiftUI

struct HeroSection: View {
    var body: some View {
        ZStack(alignment: .trailing) {
            LinearGradient(colors: [Color(hex: 0x001A2F), Color(hex: 0x003A67), Color(hex: 0x032D5B)], startPoint: .topLeading, endPoint: .bottomTrailing)

            LinearGradient(colors: [Palette.primary.opacity(0.9), Palette.primary.opacity(0.4), .clear], startPoint: .trailing, endPoint: .leading)

            VStack(alignment: .trailing, spacing: 16) {
                Text("برنامه ویژه")
                    .font(.vazir(9))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .background(Palette.secondary.opacity(0.8), in: RoundedRectangle(cornerRadius: 2, style: .continuous))

                Text("گلهای رنگارنگ")
                    .font(.vazir(27, .bold))
                    .foregroundStyle(.white)

                Text("بشنوید آثاری جاودانه از استاد محمدرضا شجریان و غلامحسین بنان در مجموعه‌ای\nبی‌نظیر که روح هر شنونده‌ای را جلا می‌دهد.")
                    .font(.vazir(13.5))
                    .foregroundStyle(.white.opacity(0.8))
                    .multilineTextAlignment(.trailing)
                    .lineSpacing(6)

                HStack(spacing: 16) {
                    Button(action: {}) {
                        Text("مشاهده لیست پخش")
                            .font(.vazir(12))
                            .foregroundStyle(.white)
                            .padding(.horizontal, 33)
                            .padding(.vertical, 13)
                            .background(Color.white.opacity(0.10), in: RoundedRectangle(cornerRadius: 12, style: .continuous))
                            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.white.opacity(0.20), lineWidth: 1))
                    }
                    .buttonStyle(.plain)

                    Button(action: {}) {
                        HStack(spacing: 8) {
                            Text("پخش آخرین قسمت")
                                .font(.vazir(12))
                            Image(systemName: "play.fill")
                                .font(.system(size: 12, weight: .bold))
                        }
                        .foregroundStyle(.white)
                        .padding(.horizontal, 32)
                        .padding(.vertical, 12.5)
                        .background(Palette.secondary, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
                    }
                    .buttonStyle(.plain)
                }
            }
            .frame(width: 564, alignment: .trailing)
            .padding(.trailing, 64)
        }
        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
    }
}
