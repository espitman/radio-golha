import SwiftUI

private enum DesktopPage {
    case home
    case singers
}

struct HomeRootView: View {
    @State private var currentPage: DesktopPage = .home

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
        Group {
            switch currentPage {
            case .home:
                MainContentSection { tab in
                    if tab == .artists {
                        currentPage = .singers
                    }
                }
            case .singers:
                SingersContentView { tab in
                    if tab == .programs {
                        currentPage = .home
                    }
                }
            }
        }
    }
}
