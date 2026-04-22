import Foundation
import CryptoKit

struct DesktopDatabaseManifest: Decodable {
    let fileName: String
    let sha256: String
    let sizeBytes: Int
    let releasedAt: String
}

struct DesktopDatabaseUpdateResult {
    let didUpdate: Bool
    let destinationPath: String
    let releasedAt: String
    let sizeBytes: Int
    let sha256: String
}

enum DesktopDatabaseUpdater {
    private static let defaultManifestURLString = "https://storage.iran.liara.space/espitman/golha/db/database_manifest.json"
    private static let noCacheSession: URLSession = {
        let config = URLSessionConfiguration.ephemeral
        config.requestCachePolicy = .reloadIgnoringLocalCacheData
        config.urlCache = nil
        return URLSession(configuration: config)
    }()

    static func updateFromCdn(
        forceDownload: Bool = false,
        onDownloadProgress: @escaping @Sendable (Double) -> Void = { _ in }
    ) async throws -> DesktopDatabaseUpdateResult {
        let manifestURL = appendCacheBuster(try resolveManifestURL())
        let manifest = try await loadManifest(from: manifestURL)
        let root = try resolveRepoRoot()
        let dbPath = try resolveArchiveDbPath(root: root)
        let destinationURL = URL(fileURLWithPath: dbPath)

        if !forceDownload,
           let currentHash = try localDatabaseHashIfExists(at: destinationURL),
           currentHash.lowercased() == manifest.sha256.lowercased() {
            return DesktopDatabaseUpdateResult(
                didUpdate: false,
                destinationPath: destinationURL.path,
                releasedAt: manifest.releasedAt,
                sizeBytes: manifest.sizeBytes,
                sha256: manifest.sha256
            )
        }

        let dbURL = appendCacheBuster(try resolveDatabaseURL(manifestURL: manifestURL, fileName: manifest.fileName))
        let dbData = try await downloadDatabase(
            from: dbURL,
            expectedSize: manifest.sizeBytes,
            onDownloadProgress: onDownloadProgress
        )

        guard dbData.count == manifest.sizeBytes else {
            throw NSError(
                domain: "DesktopDatabaseUpdater",
                code: 30,
                userInfo: [NSLocalizedDescriptionKey: "اندازه فایل دیتابیس با مانیفست هم‌خوانی ندارد."]
            )
        }

        let hash = sha256Hex(dbData)
        guard hash.lowercased() == manifest.sha256.lowercased() else {
            throw NSError(
                domain: "DesktopDatabaseUpdater",
                code: 31,
                userInfo: [NSLocalizedDescriptionKey: "هش دیتابیس صحیح نیست و فایل جایگزین نشد."]
            )
        }

        guard dbData.starts(with: Array("SQLite format 3".utf8)) else {
            throw NSError(
                domain: "DesktopDatabaseUpdater",
                code: 32,
                userInfo: [NSLocalizedDescriptionKey: "فایل دریافت‌شده دیتابیس SQLite معتبر نیست."]
            )
        }

        let tempURL = destinationURL.deletingLastPathComponent().appendingPathComponent("\(destinationURL.lastPathComponent).download")

        try dbData.write(to: tempURL, options: .atomic)

        let fm = FileManager.default
        if fm.fileExists(atPath: destinationURL.path) {
            _ = try fm.replaceItemAt(destinationURL, withItemAt: tempURL, backupItemName: nil, options: .usingNewMetadataOnly)
        } else {
            try fm.moveItem(at: tempURL, to: destinationURL)
        }

        return DesktopDatabaseUpdateResult(
            didUpdate: true,
            destinationPath: destinationURL.path,
            releasedAt: manifest.releasedAt,
            sizeBytes: manifest.sizeBytes,
            sha256: manifest.sha256
        )
    }

    private static func resolveManifestURL() throws -> URL {
        let fromEnv = ProcessInfo.processInfo.environment["RADIOGOLHA_DB_MANIFEST_URL"]?.trimmingCharacters(in: .whitespacesAndNewlines)
        let raw: String
        if let fromEnv, !fromEnv.isEmpty {
            raw = fromEnv
        } else {
            raw = defaultManifestURLString
        }
        guard let url = URL(string: raw) else {
            throw NSError(
                domain: "DesktopDatabaseUpdater",
                code: 40,
                userInfo: [NSLocalizedDescriptionKey: "آدرس مانیفست دیتابیس نامعتبر است."]
            )
        }
        return url
    }

    private static func resolveDatabaseURL(manifestURL: URL, fileName: String) throws -> URL {
        if let override = ProcessInfo.processInfo.environment["RADIOGOLHA_DB_FILE_URL"]?.trimmingCharacters(in: .whitespacesAndNewlines),
           !override.isEmpty,
           let url = URL(string: override) {
            return url
        }
        return manifestURL.deletingLastPathComponent().appendingPathComponent(fileName)
    }

    private static func loadManifest(from manifestURL: URL) async throws -> DesktopDatabaseManifest {
        var request = URLRequest(url: manifestURL)
        request.cachePolicy = .reloadIgnoringLocalAndRemoteCacheData
        request.setValue("no-cache", forHTTPHeaderField: "Cache-Control")
        request.setValue("no-cache", forHTTPHeaderField: "Pragma")
        let (data, _) = try await noCacheSession.data(for: request)
        do {
            return try JSONDecoder().decode(DesktopDatabaseManifest.self, from: data)
        } catch {
            throw NSError(
                domain: "DesktopDatabaseUpdater",
                code: 20,
                userInfo: [NSLocalizedDescriptionKey: "خواندن مانیفست دیتابیس ناموفق بود."]
            )
        }
    }

    private static func downloadDatabase(
        from url: URL,
        expectedSize: Int,
        onDownloadProgress: @escaping @Sendable (Double) -> Void
    ) async throws -> Data {
        var request = URLRequest(url: url)
        request.cachePolicy = .reloadIgnoringLocalAndRemoteCacheData
        request.setValue("no-cache", forHTTPHeaderField: "Cache-Control")
        request.setValue("no-cache", forHTTPHeaderField: "Pragma")
        let (bytes, response) = try await noCacheSession.bytes(for: request)
        if let httpResponse = response as? HTTPURLResponse, !(200...299).contains(httpResponse.statusCode) {
            throw NSError(
                domain: "DesktopDatabaseUpdater",
                code: 21,
                userInfo: [NSLocalizedDescriptionKey: "دانلود دیتابیس ناموفق بود."]
            )
        }

        let knownLength = response.expectedContentLength > 0 ? Int(response.expectedContentLength) : expectedSize
        let total = max(knownLength, expectedSize, 1)
        var data = Data()
        data.reserveCapacity(expectedSize)

        var received = 0
        var nextEmitAt = 0
        onDownloadProgress(0)
        for try await byte in bytes {
            data.append(byte)
            received += 1
            if received >= nextEmitAt {
                let progress = min(1.0, Double(received) / Double(total))
                onDownloadProgress(progress)
                nextEmitAt = received + 64 * 1024
            }
        }
        onDownloadProgress(1)
        return data
    }

    private static func resolveRepoRoot() throws -> String {
        let fileManager = FileManager.default
        let seeds = [
            fileManager.currentDirectoryPath,
            URL(fileURLWithPath: #filePath).deletingLastPathComponent().path
        ]

        for seed in seeds {
            var current = URL(fileURLWithPath: seed)
            while true {
                let marker = current.appendingPathComponent("core").path
                if fileManager.fileExists(atPath: marker) {
                    return current.path
                }

                let parent = current.deletingLastPathComponent()
                if parent.path == current.path {
                    break
                }
                current = parent
            }
        }

        throw NSError(
            domain: "DesktopDatabaseUpdater",
            code: 10,
            userInfo: [NSLocalizedDescriptionKey: "ریشه پروژه پیدا نشد."]
        )
    }

    private static func resolveArchiveDbPath(root: String) throws -> String {
        if let env = ProcessInfo.processInfo.environment["RADIOGOLHA_ARCHIVE_DB"], !env.isEmpty {
            return env
        }

        let candidate = URL(fileURLWithPath: root).appendingPathComponent("database/golha_database.db").path
        if FileManager.default.fileExists(atPath: candidate) {
            return candidate
        }

        throw NSError(
            domain: "DesktopDatabaseUpdater",
            code: 11,
            userInfo: [NSLocalizedDescriptionKey: "فایل دیتابیس آرشیو پیدا نشد."]
        )
    }

    private static func localDatabaseHashIfExists(at url: URL) throws -> String? {
        guard FileManager.default.fileExists(atPath: url.path) else {
            return nil
        }
        let data = try Data(contentsOf: url)
        guard !data.isEmpty else { return nil }
        return sha256Hex(data)
    }

    private static func sha256Hex(_ data: Data) -> String {
        SHA256.hash(data: data).compactMap { String(format: "%02x", $0) }.joined()
    }

    private static func appendCacheBuster(_ url: URL) -> URL {
        guard var components = URLComponents(url: url, resolvingAgainstBaseURL: false) else {
            return url
        }
        var queryItems = components.queryItems ?? []
        queryItems.removeAll { $0.name == "_ts" }
        queryItems.append(URLQueryItem(name: "_ts", value: String(Int(Date().timeIntervalSince1970))))
        components.queryItems = queryItems
        return components.url ?? url
    }
}
