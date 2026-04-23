import Foundation

enum DesktopFavoriteArtistsDataLoader {
    static func loadFavoriteArtistIds(artistType: String? = nil) async -> Set<Int64> {
        await Task.detached(priority: .userInitiated) {
            (try? loadFavoriteArtistIdsSync(artistType: artistType)) ?? []
        }.value
    }

    static func addFavoriteArtist(artistId: Int64, artistType: String) async -> Bool {
        await Task.detached(priority: .userInitiated) {
            (try? addFavoriteArtistSync(artistId: artistId, artistType: artistType)) ?? false
        }.value
    }

    static func removeFavoriteArtist(artistId: Int64) async -> Bool {
        await Task.detached(priority: .userInitiated) {
            (try? removeFavoriteArtistSync(artistId: artistId)) ?? false
        }.value
    }

    private static func loadFavoriteArtistIdsSync(artistType: String?) throws -> Set<Int64> {
        let root = try resolveRepoRoot()
        let userDbPath = try resolveUserDbPath()
        var args = ["get-favorite-artist-ids-json", "--user-db", userDbPath]
        if let artistType, !artistType.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            args.append(contentsOf: ["--artist-type", artistType])
        }
        let payload = try runBridge(root: root, arguments: args)
        guard !payload.isEmpty, !payload.contains("\"error\"") else { return [] }
        let decoded = try JSONDecoder().decode([Int64].self, from: Data(payload.utf8))
        return Set(decoded)
    }

    private static func addFavoriteArtistSync(artistId: Int64, artistType: String) throws -> Bool {
        let root = try resolveRepoRoot()
        let userDbPath = try resolveUserDbPath()
        let payload = try runBridge(
            root: root,
            arguments: [
                "add-favorite-artist",
                "--user-db", userDbPath,
                "--artist-id", String(artistId),
                "--artist-type", artistType
            ]
        )
        return payload == "ok"
    }

    private static func removeFavoriteArtistSync(artistId: Int64) throws -> Bool {
        let root = try resolveRepoRoot()
        let userDbPath = try resolveUserDbPath()
        let payload = try runBridge(
            root: root,
            arguments: [
                "remove-favorite-artist",
                "--user-db", userDbPath,
                "--artist-id", String(artistId)
            ]
        )
        return payload == "ok"
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
            domain: "DesktopFavoriteArtistsDataLoader",
            code: 1,
            userInfo: [NSLocalizedDescriptionKey: "Could not resolve repository root"]
        )
    }

    private static func resolveUserDbPath() throws -> String {
        if let env = ProcessInfo.processInfo.environment["RADIOGOLHA_USER_DB"], !env.isEmpty {
            return env
        }

        let fm = FileManager.default
        let appSupport = try fm.url(
            for: .applicationSupportDirectory,
            in: .userDomainMask,
            appropriateFor: nil,
            create: true
        )
        let dir = appSupport.appendingPathComponent("RadioGolhaDesktop", isDirectory: true)
        if !fm.fileExists(atPath: dir.path) {
            try fm.createDirectory(at: dir, withIntermediateDirectories: true)
        }
        return dir.appendingPathComponent("user_data.db").path
    }

    private static func runBridge(root: String, arguments: [String]) throws -> String {
        let manifest = URL(fileURLWithPath: root)
            .appendingPathComponent("core/adapters/macos/Cargo.toml").path
        let binary = URL(fileURLWithPath: root)
            .appendingPathComponent("core/adapters/macos/target/debug/radiogolha-macos-bridge-cli").path

        if !FileManager.default.isExecutableFile(atPath: binary) {
            _ = try runProcess(
                launchPath: "/usr/bin/env",
                arguments: ["cargo", "build", "--manifest-path", manifest, "--bin", "radiogolha-macos-bridge-cli"],
                currentDirectory: root
            )
        }

        return try runProcess(
            launchPath: binary,
            arguments: arguments,
            currentDirectory: root
        )
    }

    private static func runProcess(
        launchPath: String,
        arguments: [String],
        currentDirectory: String
    ) throws -> String {
        let process = Process()
        process.executableURL = URL(fileURLWithPath: launchPath)
        process.arguments = arguments
        process.currentDirectoryURL = URL(fileURLWithPath: currentDirectory)

        let stdout = Pipe()
        let stderr = Pipe()
        process.standardOutput = stdout
        process.standardError = stderr
        try process.run()
        process.waitUntilExit()

        let out = String(
            data: stdout.fileHandleForReading.readDataToEndOfFile(),
            encoding: .utf8
        ) ?? ""
        if process.terminationStatus == 0 {
            return out.trimmingCharacters(in: .whitespacesAndNewlines)
        }

        let err = String(
            data: stderr.fileHandleForReading.readDataToEndOfFile(),
            encoding: .utf8
        ) ?? "Unknown process error"
        throw NSError(
            domain: "DesktopFavoriteArtistsDataLoader",
            code: Int(process.terminationStatus),
            userInfo: [NSLocalizedDescriptionKey: err]
        )
    }
}
