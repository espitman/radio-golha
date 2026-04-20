import SwiftUI

private enum DesktopPage {
    case home
    case singers
    case players
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
                selectedTab: selectedTab
            ) { tab in
                switch tab {
                case .artists:
                    currentPage = .singers
                case .instrumentalists:
                    currentPage = .players
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
                case .players:
                    PlayersContentView()
                }
            }
            .frame(maxHeight: .infinity)
        }
    }

    private var selectedTab: DesktopMainTab {
        switch currentPage {
        case .home:
            return .programs
        case .singers:
            return .artists
        case .players:
            return .instrumentalists
        }
    }
}
