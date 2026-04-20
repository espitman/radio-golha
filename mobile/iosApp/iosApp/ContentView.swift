import SwiftUI
import RadioGolhaMobile

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        NSLog("GOLHA_SWIFT: Creating MainViewController")
        let bundlePath = Bundle.main.path(forResource: "golha_database", ofType: "db") ?? ""
        return MainViewControllerKt.MainViewController(bundlePath: bundlePath)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(.all) // Compose handles safe area
    }
}
