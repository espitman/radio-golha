import SwiftUI
import AppKit

final class DesktopAppDelegate: NSObject, NSApplicationDelegate {
    func applicationDidFinishLaunching(_ notification: Notification) {
        NSApp.setActivationPolicy(.regular)
        NSApp.activate(ignoringOtherApps: true)

        // Ensure window chrome is minimized for a frameless desktop look.
        DispatchQueue.main.async {
            NSApp.windows.forEach { window in
                window.titleVisibility = .hidden
                window.titlebarAppearsTransparent = true
                window.toolbar = nil
                window.isMovableByWindowBackground = false
                window.styleMask.remove(.resizable)
                window.standardWindowButton(.zoomButton)?.isHidden = true
                window.standardWindowButton(.zoomButton)?.isEnabled = false
            }
        }
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
        .windowStyle(.hiddenTitleBar)
        .windowResizability(.contentSize)
    }
}
