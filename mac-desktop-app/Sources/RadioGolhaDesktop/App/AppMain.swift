import SwiftUI
import AppKit

final class DesktopAppDelegate: NSObject, NSApplicationDelegate {
    func applicationDidFinishLaunching(_ notification: Notification) {
        NSApp.setActivationPolicy(.regular)
        NSApp.activate(ignoringOtherApps: true)
    }
}

@main
struct RadioGolhaDesktopApp: App {
    @NSApplicationDelegateAdaptor(DesktopAppDelegate.self) private var appDelegate

    var body: some Scene {
        WindowGroup {
            HomeRootView()
                .frame(minWidth: 1280, idealWidth: 1280, maxWidth: 1280, minHeight: 800, idealHeight: 800)
        }
        .windowResizability(.contentSize)
    }
}
