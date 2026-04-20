import SwiftUI

struct HomeRootView: View {
    var body: some View {
        ZStack(alignment: .bottom) {
            Palette.surface.ignoresSafeArea()

            HStack(spacing: 0) {
                mainCanvas
                    .frame(width: 1024)
                    .environment(\.layoutDirection, .rightToLeft)
                SidebarSection()
                    .frame(width: 256)
                    .environment(\.layoutDirection, .rightToLeft)
            }
            .frame(width: 1280)
            .environment(\.layoutDirection, .leftToRight)

            BottomPlayerSection()
        }
        .environment(\.layoutDirection, .rightToLeft)
    }

    private var mainCanvas: some View {
        MainContentSection()
    }
}
