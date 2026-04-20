import SwiftUI

@main
struct RadioGolhaDesktopApp: App {
    var body: some Scene {
        WindowGroup {
            HomeRootView()
                .frame(minWidth: 1280, idealWidth: 1280, maxWidth: 1280, minHeight: 800, idealHeight: 800)
        }
        .windowResizability(.contentSize)
    }
}
