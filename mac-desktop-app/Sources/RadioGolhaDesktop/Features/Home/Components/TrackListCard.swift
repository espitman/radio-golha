import SwiftUI

struct TrackListCard: View {
    let title: String
    let rows: [TrackRowItem]

    var body: some View {
        VStack(alignment: .trailing, spacing: 12) {
            HStack {
                Image(systemName: "arrow.counterclockwise")
                    .foregroundStyle(Palette.secondary)
                    .font(.system(size: 12, weight: .medium))

                Spacer(minLength: 0)

                Text(title)
                    .font(.vazir(27))
                    .foregroundStyle(Color(hex: 0x003F8D))
            }

            VStack(spacing: 0) {
                ForEach(Array(rows.enumerated()), id: \.offset) { index, row in
                    HStack(spacing: 8) {
                        Image(systemName: "play.fill")
                            .font(.system(size: 9, weight: .bold))
                            .foregroundStyle(Palette.primary)

                        Spacer(minLength: 0)

                        VStack(alignment: .trailing, spacing: 2) {
                            Text(row.title)
                                .font(.vazir(10.5))
                                .foregroundStyle(Palette.primary)
                            Text(row.subtitle)
                                .font(.vazir(9))
                                .foregroundStyle(Palette.textMuted)
                        }

                        Text(row.duration)
                            .font(.vazir(9))
                            .foregroundStyle(Color(hex: 0xA8A29E))
                            .frame(width: 40, alignment: .leading)
                    }
                    .padding(.horizontal, 16)
                    .frame(height: 72)

                    if index < rows.count - 1 {
                        Divider().opacity(0.2)
                    }
                }
            }
            .background(Palette.surface, in: RoundedRectangle(cornerRadius: 10, style: .continuous))
        }
        .frame(width: 440)
    }
}
