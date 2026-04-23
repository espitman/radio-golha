import Foundation

enum TrackCollectionsDataLoader {
    static func loadMostPlayed(limit: Int = 20) async -> [TrackRowItem] {
        await Task.detached(priority: .userInitiated) {
            (try? loadMostPlayedSync(limit: limit)) ?? []
        }.value
    }

    static func loadRecentlyPlayed(limit: Int = 20) async -> [TrackRowItem] {
        await Task.detached(priority: .userInitiated) {
            (try? loadRecentlyPlayedSync(limit: limit)) ?? []
        }.value
    }

    static func loadByTrackIds(_ ids: [Int64]) async -> [TrackRowItem] {
        await Task.detached(priority: .userInitiated) {
            (try? loadByTrackIdsSync(ids)) ?? []
        }.value
    }

    static func loadDuetPrograms(singer1: String, singer2: String) async -> [TrackRowItem] {
        await Task.detached(priority: .userInitiated) {
            (try? loadDuetProgramsSync(singer1: singer1, singer2: singer2)) ?? []
        }.value
    }

    private static func loadMostPlayedSync(limit: Int) throws -> [TrackRowItem] {
        let root = try resolveRepoRoot()
        let userDbPath = try resolveUserDbPath()
        let archiveDbPath = try resolveArchiveDbPath(root: root)
        let idsPayload = try runBridgeMostPlayedIds(root: root, userDbPath: userDbPath, limit: limit)
        guard !idsPayload.isEmpty else { return [] }
        let ids = try JSONDecoder().decode([Int64].self, from: Data(idsPayload.utf8))
        return try loadTracks(root: root, archiveDbPath: archiveDbPath, ids: ids)
    }

    private static func loadRecentlyPlayedSync(limit: Int) throws -> [TrackRowItem] {
        let root = try resolveRepoRoot()
        let userDbPath = try resolveUserDbPath()
        let archiveDbPath = try resolveArchiveDbPath(root: root)
        let idsPayload = try runBridgeRecentlyPlayedIds(root: root, userDbPath: userDbPath, limit: limit)
        guard !idsPayload.isEmpty else { return [] }
        let ids = try JSONDecoder().decode([Int64].self, from: Data(idsPayload.utf8))
        return try loadTracks(root: root, archiveDbPath: archiveDbPath, ids: ids)
    }

    private static func loadByTrackIdsSync(_ ids: [Int64]) throws -> [TrackRowItem] {
        let root = try resolveRepoRoot()
        let archiveDbPath = try resolveArchiveDbPath(root: root)
        return try loadTracks(root: root, archiveDbPath: archiveDbPath, ids: ids)
    }

    private static func loadDuetProgramsSync(singer1: String, singer2: String) throws -> [TrackRowItem] {
        let root = try resolveRepoRoot()
        let archiveDbPath = try resolveArchiveDbPath(root: root)
        let payload = try runBridgeDuetPrograms(root: root, dbPath: archiveDbPath, singer1: singer1, singer2: singer2)
        guard !payload.isEmpty else { return [] }

        let response = try JSONDecoder().decode([ProgramByIdTrackDTO].self, from: Data(payload.utf8))
        return response.map { item in
            let trimmedTitle = item.title?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            return TrackRowItem(
                id: "track-\(item.id)",
                trackId: item.id,
                title: trimmedTitle.isEmpty ? "برنامه \(item.no)" : trimmedTitle,
                subtitle: item.artist,
                duration: normalizedDuration(item.duration),
                audioURL: item.audioUrl,
                artworkURLs: item.artistImages ?? []
            )
        }
    }

    private static func loadTracks(root: String, archiveDbPath: String, ids: [Int64]) throws -> [TrackRowItem] {
        guard !ids.isEmpty else { return [] }
        let idsJson = try String(data: JSONEncoder().encode(ids), encoding: .utf8) ?? "[]"
        let payload = try runBridgeProgramsByIds(root: root, dbPath: archiveDbPath, idsJson: idsJson)
        guard !payload.isEmpty else { return [] }

        let mapped: [TrackRowItem] = try JSONDecoder().decode([ProgramByIdTrackDTO].self, from: Data(payload.utf8)).map { item in
            let trimmedTitle = item.title?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            return TrackRowItem(
                id: "track-\(item.id)",
                trackId: item.id,
                title: trimmedTitle.isEmpty ? "برنامه \(item.no)" : trimmedTitle,
                subtitle: item.artist,
                duration: normalizedDuration(item.duration),
                audioURL: item.audioUrl,
                artworkURLs: item.artistImages ?? []
            )
        }

        let order = Dictionary(uniqueKeysWithValues: ids.enumerated().map { ($1, $0) })
        return mapped.sorted { lhs, rhs in
            let li = order[lhs.trackId ?? -1] ?? Int.max
            let ri = order[rhs.trackId ?? -1] ?? Int.max
            return li < ri
        }
    }

    private static func normalizedDuration(_ value: String?) -> String {
        guard let value, !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            return "۰۰:۰۰"
        }
        return value
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
        throw NSError(domain: "TrackCollectionsDataLoader", code: 1, userInfo: [NSLocalizedDescriptionKey: "Could not resolve repository root"])
    }

    private static func resolveArchiveDbPath(root: String) throws -> String {
        if let env = ProcessInfo.processInfo.environment["RADIOGOLHA_ARCHIVE_DB"], !env.isEmpty {
            return env
        }

        let candidate = URL(fileURLWithPath: root).appendingPathComponent("database/golha_database.db").path
        if FileManager.default.fileExists(atPath: candidate) {
            return candidate
        }

        throw NSError(domain: "TrackCollectionsDataLoader", code: 2, userInfo: [NSLocalizedDescriptionKey: "Archive DB not found"])
    }

    private static func resolveUserDbPath() throws -> String {
        if let env = ProcessInfo.processInfo.environment["RADIOGOLHA_USER_DB"], !env.isEmpty {
            return env
        }

        let fm = FileManager.default
        let appSupport = try fm.url(for: .applicationSupportDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
        let dir = appSupport.appendingPathComponent("RadioGolhaDesktop", isDirectory: true)
        if !fm.fileExists(atPath: dir.path) {
            try fm.createDirectory(at: dir, withIntermediateDirectories: true)
        }
        return dir.appendingPathComponent("user_data.db").path
    }

    private static func runBridgeMostPlayedIds(root: String, userDbPath: String, limit: Int) throws -> String {
        let binary = try ensureBridgeBinary(root: root)
        return try runProcess(
            launchPath: binary,
            arguments: ["most-played-ids-json", "--user-db", userDbPath, "--limit", String(limit)],
            currentDirectory: root
        )
    }

    private static func runBridgeRecentlyPlayedIds(root: String, userDbPath: String, limit: Int) throws -> String {
        let binary = try ensureBridgeBinary(root: root)
        return try runProcess(
            launchPath: binary,
            arguments: ["recently-played-ids-json", "--user-db", userDbPath, "--limit", String(limit)],
            currentDirectory: root
        )
    }

    private static func runBridgeProgramsByIds(root: String, dbPath: String, idsJson: String) throws -> String {
        let binary = try ensureBridgeBinary(root: root)
        return try runProcess(
            launchPath: binary,
            arguments: ["programs-by-ids-json", "--db", dbPath, "--ids-json", idsJson],
            currentDirectory: root
        )
    }

    private static func runBridgeDuetPrograms(root: String, dbPath: String, singer1: String, singer2: String) throws -> String {
        let binary = try ensureBridgeBinary(root: root)
        return try runProcess(
            launchPath: binary,
            arguments: ["duet-programs-json", "--db", dbPath, "--singer1", singer1, "--singer2", singer2],
            currentDirectory: root
        )
    }

    private static func ensureBridgeBinary(root: String) throws -> String {
        let manifest = URL(fileURLWithPath: root).appendingPathComponent("core/adapters/macos/Cargo.toml").path
        let binary = URL(fileURLWithPath: root).appendingPathComponent("core/adapters/macos/target/debug/radiogolha-macos-bridge-cli").path
        if !FileManager.default.isExecutableFile(atPath: binary) {
            _ = try runProcess(
                launchPath: "/usr/bin/env",
                arguments: ["cargo", "build", "--manifest-path", manifest, "--bin", "radiogolha-macos-bridge-cli"],
                currentDirectory: root
            )
        }
        return binary
    }

    private static func runProcess(launchPath: String, arguments: [String], currentDirectory: String) throws -> String {
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

        let out = String(data: stdout.fileHandleForReading.readDataToEndOfFile(), encoding: .utf8) ?? ""
        if process.terminationStatus == 0 {
            return out.trimmingCharacters(in: .whitespacesAndNewlines)
        }
        let err = String(data: stderr.fileHandleForReading.readDataToEndOfFile(), encoding: .utf8) ?? "Unknown process error"
        throw NSError(domain: "TrackCollectionsDataLoader", code: Int(process.terminationStatus), userInfo: [NSLocalizedDescriptionKey: err])
    }
}

private struct ProgramByIdTrackDTO: Decodable {
    let id: Int64
    let title: String?
    let no: Int64
    let artist: String
    let duration: String?
    let audioUrl: String?
    let artistImages: [String]?
}
