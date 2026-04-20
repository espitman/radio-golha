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
        VStack(spacing: 0) {
            DesktopTopNavigationBar(
                selectedTab: currentPage == .home ? .programs : .artists
            ) { tab in
                switch tab {
                case .artists:
                    currentPage = .singers
                case .programs:
                    currentPage = .home
                default:
                    break
                }
            }

            Group {
                switch currentPage {
                case .home:
                    MainContentSection()
                case .singers:
                    SingersContentView()
                }
            }
            .frame(maxHeight: .infinity)
        }
    }
}
