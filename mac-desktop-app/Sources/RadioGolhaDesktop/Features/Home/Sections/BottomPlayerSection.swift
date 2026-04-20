import SwiftUI

struct BottomPlayerSection: View {
    private let albumImage = "https://www.figma.com/api/mcp/asset/15e24aa5-c875-43d0-80d4-c65acc0e489f"
    private let iconVolume = "https://www.figma.com/api/mcp/asset/0e7e3e85-8034-4f0f-a3ef-b136c52551aa"
    private let iconPlaylist = "https://www.figma.com/api/mcp/asset/6352d5ca-99ec-4d76-9081-b36838719850"
    private let iconShuffle = "https://www.figma.com/api/mcp/asset/03fbbe64-2f84-4033-b718-4819ae5301b9"
    private let iconPrev = "https://www.figma.com/api/mcp/asset/c98503f5-be98-4ccf-a766-959862e40b24"
    private let iconPlay = "https://www.figma.com/api/mcp/asset/e485bc14-97cb-4d9f-839a-cc326860ecb4"
    private let iconNext = "https://www.figma.com/api/mcp/asset/84dfe2c7-4882-4467-8150-e6a89ae5ea9b"
    private let iconRepeat = "https://www.figma.com/api/mcp/asset/0e1233c7-9f43-48f7-84d9-bd1bb8212898"

    var body: some View {
        ZStack(alignment: .top) {
            Palette.primary
                .shadow(color: Color(red: 28/255, green: 28/255, blue: 23/255, opacity: 0.08), radius: 20, x: 0, y: -4)

            HStack(spacing: 0) {
                HStack(spacing: 16) {
                    HStack(spacing: 4) {
                        ForEach([8, 20, 32, 16, 24, 12], id: \.self) { h in
                            RoundedRectangle(cornerRadius: 12)
                                .fill(Palette.secondary)
                                .frame(width: 4, height: CGFloat(h))
                        }
                    }
                    .frame(width: 44, height: 32)
                    .opacity(0.4)

                    HStack(spacing: 8) {
                        FigmaAssetImage(url: iconPlaylist, fallbackSymbol: "music.note.list")
                            .frame(width: 14.25, height: 11.25)
                        FigmaAssetImage(url: iconVolume, fallbackSymbol: "speaker.wave.2.fill")
                            .frame(width: 13.5, height: 13.125)
                    }
                    .padding(.leading, 25)
                    .overlay(alignment: .leading) {
                        Rectangle().fill(Color.white.opacity(0.1)).frame(width: 1)
                    }
                }
                .frame(width: 394.6667, alignment: .trailing)
                .padding(.trailing, 265.66)

                HStack(spacing: 24) {
                    FigmaAssetImage(url: iconShuffle, fallbackSymbol: "shuffle").frame(width: 18, height: 20)
                    FigmaAssetImage(url: iconPrev, fallbackSymbol: "backward.end.fill").frame(width: 16.25, height: 15)
                    ZStack {
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .fill(.white)
                            .frame(width: 48, height: 48)
                        FigmaAssetImage(url: iconPlay, fallbackSymbol: "play.fill", fallbackTint: Palette.primary)
                            .frame(width: 13.75, height: 17.5)
                    }
                    FigmaAssetImage(url: iconNext, fallbackSymbol: "forward.end.fill").frame(width: 16.25, height: 15)
                    FigmaAssetImage(url: iconRepeat, fallbackSymbol: "repeat").frame(width: 16, height: 16)
                }
                .frame(width: 394.6667, alignment: .center)

                HStack(spacing: 16) {
                    VStack(alignment: .trailing, spacing: 0) {
                        Text("در حال پخش")
                            .font(.vazir(7.5))
                            .foregroundStyle(Palette.secondary)
                            .tracking(1)
                        Text("آستان جانان - آواز شور")
                            .font(.vazir(10.5))
                            .foregroundStyle(.white)
                        Text("محمدرضا شجریان")
                            .font(.vazir(7.5))
                            .foregroundStyle(.white.opacity(0.6))
                    }

                    RoundedRectangle(cornerRadius: 2, style: .continuous)
                        .fill(Color.white.opacity(0.1))
                        .overlay {
                            FigmaAssetImage(url: albumImage, fallbackSymbol: "music.note", fallbackTint: .white.opacity(0.85))
                        }
                        .frame(width: 48, height: 48)
                }
                .frame(width: 394.6667, alignment: .leading)
                .padding(.leading, 203.6)
            }
            .frame(width: 1184, height: 96)
            .padding(.horizontal, 48)
            .environment(\.layoutDirection, .leftToRight)

            Rectangle()
                .fill(.white.opacity(0.1))
                .frame(width: 1280, height: 4)

            Rectangle()
                .fill(Palette.secondary)
                .frame(width: 426.66, height: 4)
                .frame(maxWidth: .infinity, alignment: .trailing)
        }
        .frame(width: 1280, height: 96)
    }
}
