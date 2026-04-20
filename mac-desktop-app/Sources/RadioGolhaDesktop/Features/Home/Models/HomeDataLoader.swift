import Foundation

enum HomeDataLoader {
    static func load() async -> HomeContentData {
        await Task.detached(priority: .userInitiated) {
            (try? loadSync()) ?? .mock
        }.value
    }

    static func loadTopTracksRows() async -> (top: [TrackRowItem], latest: [TrackRowItem])? {
        await Task.detached(priority: .userInitiated) {
            try? loadTopTracksRowsSync()
        }.value
    }

    private static func loadSync() throws -> HomeContentData {
        let root = try resolveRepoRoot()
        let dbPath = try resolveArchiveDbPath(root: root)
        let payload = try runBridgeHomeFeed(root: root, dbPath: dbPath)
        guard !payload.isEmpty else { return .mock }

        let response = try JSONDecoder().decode(HomeFeedBridgeResponse.self, from: Data(payload.utf8))
        return mapResponse(response)
    }

    private static func loadTopTracksRowsSync() throws -> (top: [TrackRowItem], latest: [TrackRowItem]) {
        let root = try resolveRepoRoot()
        let dbPath = try resolveArchiveDbPath(root: root)
        let payload = try runBridgeTopTracks(root: root, dbPath: dbPath)
        if payload.isEmpty {
            return (HomeMockData.topProgramsRows, HomeMockData.latestTracksRows)
        }

        let tracks = try JSONDecoder().decode([HomeTrackDTO].self, from: Data(payload.utf8)).map {
            TrackRowItem(
                title: $0.title,
                subtitle: $0.artist,
                duration: normalizedDuration($0.duration)
            )
        }

        let top = Array(tracks.prefix(5))
        let latest: [TrackRowItem] = {
            let next = Array(tracks.dropFirst(5).prefix(5))
            return next.isEmpty ? top : next
        }()
        return (
            top.isEmpty ? HomeMockData.topProgramsRows : top,
            latest.isEmpty ? HomeMockData.latestTracksRows : latest
        )
    }

    private static func mapResponse(_ response: HomeFeedBridgeResponse) -> HomeContentData {
        let categories = response.categories
            .enumerated()
            .map { index, category in
                ProgramItem(
                    title: category.title,
                    count: "\(category.episodeCount) برنامه",
                    symbol: symbol(for: category.title, fallbackIndex: index)
                )
            }

        let singers = response.singers.map {
            ArtistItem(
                name: $0.name,
                role: "خواننده",
                imageURL: $0.avatar ?? ""
            )
        }

        let musicians = response.musicians.map {
            ArtistItem(
                name: $0.name,
                role: $0.instrument?.isEmpty == false ? ($0.instrument ?? "نوازنده") : "نوازنده",
                imageURL: $0.avatar ?? ""
            )
        }

        let modes = response.dastgahs.map { ModeItem(title: $0.name) }

        let tracks = response.topTracks.map {
            TrackRowItem(
                title: $0.title,
                subtitle: $0.artist,
                duration: normalizedDuration($0.duration)
            )
        }

        let topProgramsRows = Array(tracks.prefix(5))
        let latestTracksRows: [TrackRowItem] = {
            let next = Array(tracks.dropFirst(5).prefix(5))
            return next.isEmpty ? topProgramsRows : next
        }()

        return HomeContentData(
            programs: categories.isEmpty ? HomeMockData.programs : categories,
            singers: singers.isEmpty ? HomeMockData.singers : singers,
            instrumentalists: musicians.isEmpty ? HomeMockData.instrumentalists : musicians,
            modes: modes.isEmpty ? HomeMockData.modes : modes,
            topProgramsRows: topProgramsRows.isEmpty ? HomeMockData.topProgramsRows : topProgramsRows,
            latestTracksRows: latestTracksRows.isEmpty ? HomeMockData.latestTracksRows : latestTracksRows
        )
    }

    private static func normalizedDuration(_ value: String?) -> String {
        guard let value, !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            return "۰۰:۰۰"
        }
        return value
    }

    private static func symbol(for title: String, fallbackIndex: Int) -> String {
        let normalized = title.replacingOccurrences(of: "‌", with: "").lowercased()
        if normalized.contains("سبز") { return "leaf" }
        if normalized.contains("تازه") { return "seal" }
        if normalized.contains("شاخه") { return "sparkles" }
        if normalized.contains("جاوید") { return "hexagon" }
        let fallback = ["seal", "leaf", "sparkles", "hexagon"]
        return fallback[fallbackIndex % fallback.count]
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

        throw NSError(domain: "HomeDataLoader", code: 1, userInfo: [NSLocalizedDescriptionKey: "Could not resolve repository root"])
    }

    private static func resolveArchiveDbPath(root: String) throws -> String {
        if let env = ProcessInfo.processInfo.environment["RADIOGOLHA_ARCHIVE_DB"], !env.isEmpty {
            return env
        }

        let candidate = URL(fileURLWithPath: root).appendingPathComponent("database/golha_database.db").path
        if FileManager.default.fileExists(atPath: candidate) {
            return candidate
        }

        throw NSError(domain: "HomeDataLoader", code: 2, userInfo: [NSLocalizedDescriptionKey: "Archive DB not found"])
    }

    private static func runBridgeHomeFeed(root: String, dbPath: String) throws -> String {
        let manifest = URL(fileURLWithPath: root).appendingPathComponent("core/adapters/macos/Cargo.toml").path
        let binary = URL(fileURLWithPath: root).appendingPathComponent("core/adapters/macos/target/debug/radiogolha-macos-bridge-cli").path

        if !FileManager.default.isExecutableFile(atPath: binary) {
            _ = try runProcess(
                launchPath: "/usr/bin/env",
                arguments: ["cargo", "build", "--manifest-path", manifest, "--bin", "radiogolha-macos-bridge-cli"],
                currentDirectory: root
            )
        }

        return try runProcess(
            launchPath: binary,
            arguments: ["home-feed-json", "--db", dbPath],
            currentDirectory: root
        )
    }

    private static func runBridgeTopTracks(root: String, dbPath: String) throws -> String {
        let manifest = URL(fileURLWithPath: root).appendingPathComponent("core/adapters/macos/Cargo.toml").path
        let binary = URL(fileURLWithPath: root).appendingPathComponent("core/adapters/macos/target/debug/radiogolha-macos-bridge-cli").path

        if !FileManager.default.isExecutableFile(atPath: binary) {
            _ = try runProcess(
                launchPath: "/usr/bin/env",
                arguments: ["cargo", "build", "--manifest-path", manifest, "--bin", "radiogolha-macos-bridge-cli"],
                currentDirectory: root
            )
        }

        return try runProcess(
            launchPath: binary,
            arguments: ["top-tracks-json", "--db", dbPath],
            currentDirectory: root
        )
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
        throw NSError(domain: "HomeDataLoader", code: Int(process.terminationStatus), userInfo: [NSLocalizedDescriptionKey: err])
    }
}

private struct HomeFeedBridgeResponse: Decodable {
    let categories: [HomeCategoryDTO]
    let singers: [HomeSingerDTO]
    let musicians: [HomeMusicianDTO]
    let dastgahs: [HomeModeDTO]
    let topTracks: [HomeTrackDTO]
}

private struct HomeCategoryDTO: Decodable {
    let id: Int64
    let title: String
    let episodeCount: Int
}

private struct HomeSingerDTO: Decodable {
    let id: Int64
    let name: String
    let avatar: String?
    let programCount: Int?
}

private struct HomeMusicianDTO: Decodable {
    let id: Int64
    let name: String
    let avatar: String?
    let instrument: String?
    let programCount: Int?
}

private struct HomeModeDTO: Decodable {
    let name: String
}

private struct HomeTrackDTO: Decodable {
    let id: Int64
    let title: String
    let artist: String
    let duration: String?
    let audioUrl: String?
}
