import SwiftUI
import AppKit
import CryptoKit

struct CachedRemoteImage<Content: View, Placeholder: View>: View {
    let url: URL?
    let content: (Image) -> Content
    let placeholder: () -> Placeholder

    @StateObject private var loader = RemoteImageLoader()

    init(
        url: URL?,
        @ViewBuilder content: @escaping (Image) -> Content,
        @ViewBuilder placeholder: @escaping () -> Placeholder
    ) {
        self.url = url
        self.content = content
        self.placeholder = placeholder
    }

    var body: some View {
        Group {
            if let image = loader.image {
                content(Image(nsImage: image))
            } else {
                placeholder()
            }
        }
        .task(id: url) {
            await loader.load(url: url)
        }
    }
}

@MainActor
private final class RemoteImageLoader: ObservableObject {
    @Published var image: NSImage?

    func load(url: URL?) async {
        guard let url else {
            image = nil
            return
        }

        if let cached = await DesktopImageCache.shared.image(for: url) {
            image = cached
            return
        }

        do {
            let (data, _) = try await URLSession.shared.data(from: url)
            guard let loaded = NSImage(data: data) else {
                image = nil
                return
            }

            await DesktopImageCache.shared.store(data: data, image: loaded, for: url)
            image = loaded
        } catch {
            image = nil
        }
    }
}

private actor DesktopImageCache {
    static let shared = DesktopImageCache()

    private let memory = NSCache<NSString, NSImage>()
    private let directoryURL: URL

    init() {
        let fm = FileManager.default
        let base = fm.urls(for: .cachesDirectory, in: .userDomainMask).first
            ?? URL(fileURLWithPath: NSTemporaryDirectory())
        let directory = base.appendingPathComponent("RadioGolhaDesktopImageCache", isDirectory: true)
        if !fm.fileExists(atPath: directory.path) {
            try? fm.createDirectory(at: directory, withIntermediateDirectories: true)
        }
        self.directoryURL = directory
        memory.countLimit = 300
    }

    func image(for url: URL) -> NSImage? {
        let key = url.absoluteString as NSString
        if let memoryImage = memory.object(forKey: key) {
            return memoryImage
        }

        let fileURL = diskURL(for: url.absoluteString)
        guard let data = try? Data(contentsOf: fileURL),
              let diskImage = NSImage(data: data) else {
            return nil
        }
        memory.setObject(diskImage, forKey: key)
        return diskImage
    }

    func store(data: Data, image: NSImage, for url: URL) {
        let key = url.absoluteString as NSString
        memory.setObject(image, forKey: key)
        let fileURL = diskURL(for: url.absoluteString)
        try? data.write(to: fileURL, options: .atomic)
    }

    private func diskURL(for key: String) -> URL {
        let digest = SHA256.hash(data: Data(key.utf8))
        let name = digest.map { String(format: "%02x", $0) }.joined()
        return directoryURL.appendingPathComponent(name).appendingPathExtension("img")
    }
}

